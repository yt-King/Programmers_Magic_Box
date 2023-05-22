# 在node中的使用

要针对 Neo4j 数据库执行 Cypher 语句，需要使用一个名为 Driver 的对象。

>*Driver 对象是一个线程安全的、应用程序范围的载具，所有 Neo4j 交互都派生自该载具。*
>
>*驱动程序 API 与拓扑无关，因此可以针对 Neo4j 集群或单个 DBMS 运行相同的代码。*

## 1、安装

```js
npm install --save neo4j-driver
```

## 2、建立连接（drive）

```typescript
import {Driver} from "neo4j-driver/types/driver";

const neo4j = require('neo4j-driver')
const db = process.env.host || 'bolt://10.0.2.39:7687'
const dbuser = process.env.dbuser || 'neo4j'
const dbpassword = process.env.dbpassword || 'hello123'

export class NeoRepository {
    private instance: Driver;

    async init() {
        this.instance = neo4j.driver(db, neo4j.auth.basic(dbuser, dbpassword), {
            maxTransactionRetryTime: 30000
        })
        const verifyConnectivity = await this.instance.verifyConnectivity();
        console.log('init neo driver', new Date(), "\n", verifyConnectivity);
    }

    getInstance() {
        return this.instance;
    }
}
/** 示例
init neo driver 2023-05-15T05:57:04.499Z 
 ServerInfo {
  address: '10.0.2.39:7687',
  agent: 'Neo4j/5.3.0',
  protocolVersion: 5
}
*/
```

可以通过调用 verifyConnectivity（）方法来验证驱动程序实例化期间使用的连接详细信息是否正确。此函数返回一个 Promise，如果连接详细信息正确，则解析，如果无法建立连接，则以 Neo.ClientError.Security.Unauthorized 错误拒绝。

## 3、建立会话（session）

>*会话是一系列事务的容器。会话根据需要从池中借用连接，并被视为轻量级和一次性连接。*
>
>*当驱动程序连接到数据库时，它会打开多个可由会话借用的 TCP 连接。可以通过多个连接发送查询，驱动程序可以通过多个连接接收结果。*

```typescript
// Open a new session
const session = driver.session()
```

此会话方法采用可选的配置参数，可用于设置数据库以在多数据库设置中运行任何查询，以及事务中运行的任何查询的默认访问模式（READ 或 WRITE）。

```typescript
import neo4j, { session } from 'neo4j-driver'

// Create a Session for the `people` database
const session = driver.session({
  // Run sessions in WRITE mode by default
  defaultAccessMode: session.WRITE,
  // Run all queries against the `people` database
  // 如果未提供数据库，将使用默认数据库。 这是在neo4j.conf中的dbms.default_database中配置的，默认值为neo4j。
  database: 'people',
})
```

完成会话后，调用 close() 方法释放该会话持有的所有数据库连接。

```typescript
await session.close()
```

## 4、事务

### 4.1自动事务

自动提交事务是针对 DBMS 立即执行并立即确认的单个工作单元。 您可以通过在会话对象上调用 run() 方法来运行自动提交事务，将 Cypher 语句作为字符串传递，并可选地传递包含一组参数的对象。

```typescript
// Run a query in an auto-commit transaction
const res = await session.run(query, params)
```

### 4.2读事务

会话提供了一个 executeRead() 方法，它需要一个参数，一个表示工作单元的函数。 该函数将接受一个参数，一个 Transaction 对象，您可以在该对象上调用带有两个参数的 run() 方法：作为字符串的 Cypher 语句和包含一组参数的可选对象。

```typescript
// Run a query within a Read Transaction
const res = await session.executeRead(tx => {
  return tx.run(
    `MATCH (p:Person)-[:ACTED_IN]->(m:Movie)
    WHERE m.title = $title // (1)
    RETURN p.name AS name
    LIMIT 10`,
    { title: 'Arthur' } // (2)
  )
})
```

>在上面的查询中，$title (1) 的 $ 前缀表示此值与 run() 方法调用的第二个参数 (2) 中定义的参数相关。

### 4.3写事务

类似于读事务：

```typescript
const res = await session.executeWrite(tx => {
  return tx.run(
    'CREATE (p:Person {name: $name})',
    { name: 'Michael' }
  )
})
```

如果工作单元内出现任何问题或 Neo4j 方面出现问题，事务将自动回滚，数据库将保持其先前状态。 如果工作单元成功，事务将自动提交。

### 4.4显示声明事务

可以通过在会话上调用 beginTransaction() 方法来显式创建事务对象。

```typescript
import neo4j, { session } from 'neo4j-driver'


// Open a new session
const session = driver.session({
  defaultAccessMode: session.WRITE
})

// Manually create a transaction
// 返回一个事务对象，该对象与调用 executeRead() 或 executeWrite() 时传递给工作单元函数的对象相同。
const tx = session.beginTransaction()
```

不同于 executeRead 和 executeWrite() 方法，手动声明事务必须根据工作单元的结果手动提交或回滚。

```typescript
try {
  // Perform an action
  await tx.run(query, params)

  // Commit the transaction
  await tx.commit()
}
catch (e) {
  // If something went wrong, rollback the transaction
  await tx.rollback()
}
finally {
  // Finally, close the session
  await session.close()
}
```

