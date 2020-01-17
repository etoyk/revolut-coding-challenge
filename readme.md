# Revolut Backend Test
### Requirements
Design and implement a RESTful API (including data model and the backing implementation) for
money transfers between accounts.
Explicit requirements:
1. You can use Java or Kotlin.
2. Keep it simple and to the point (e.g. no need to implement any authentication).
3. Assume the API is invoked by multiple systems and services on behalf of end users.
4. You can use frameworks/libraries if you like (except Spring), but don't forget about
requirement #2 and keep it simple and avoid heavy frameworks.
5. The datastore should run in-memory for the sake of this test.
6. The final result should be executable as a standalone program (should not require a
pre-installed container/server).
7. Demonstrate with tests that the API works as expected.
Implicit requirements:
1. The code produced by you is expected to be of high quality.
2. There are no detailed requirements, use common sense.
Please put your work on github or bitbucket.

# Solution
This is a RESTful implementation of a money transfer system in Java. 

### Used Libraries
- Javalin for REST framework
- JOOQ, hsqldb for in memory db and manipulation
- lombok for convenience
- junit, assertj, mockito for testing

### Design Decisions / Limitations
- A unique, disposable transaction id is required for transfer, deposit and withdraw operations
 to prevent double spending issue
- Everything is logged in the `Transactions` table which can be used as a source of truth for
  ledger consistency
- All important operations are transactional with rollback functionality
- Lock implementation is not suitable for distributed systems, it would work only as a monolith
- Money is not in a decimal format, it's a long with last 2 digits treated as decimal 
($10050 is 100 dollars and 50 cents) to prevent possible floating point errors
- No dependency injection framework is used because it was unnecessary


### How to build 
Java 11 and mvn is required. 

`mvn clean package
`
### How to run
`java -jar target/bank-1.0.jar`

## Endpoints
### Account
<table>
  <thead>
    <tr>
      <th>Method</th>
      <th>Endpoint</th>
      <th>Desc</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>PATCH</td>
      <td>​/accounts​/transfer​/from​/{fromId}​/to​/{toId}</td>
      <td>Transfer balance between accounts</td>
    </tr>
    <tr>
      <td>PATCH​</td>
      <td>/accounts​/deposit​/{id}</td>
      <td>Deposit balance to an account</td>
    </tr>
  <tr>
    <td>PATCH</td>
    <td>/accounts​/withdraw​/{id}</td>
    <td>Withdraw balance from an account</td>
  </tr>
  <tr>
    <td>GET</td>
    <td>/accounts</td>
    <td>Get all accounts</td>
  </tr>
  <tr>
    <td>POST</td>
    <td>/accounts</td>
    <td>Create account</td>
  </tr>
  <tr>
    <td>GET</td>
    <td>/accounts​/{id}</td>
    <td>Get account by id</td>
  </tr>
  <tr>
    <td>PUT</td>
    <td>/accounts​/{id}</td>
    <td>Update account by Id</td>
  </tr>
    <tr><td>DELETE</td>
    <td>/accounts​/{id}</td>
    <td>Delete account by Id</td>
  </tr>
  </tr>
    <tr><td>GET</td>
    <td>/transactions</td>
    <td>List all transactions</td>
  </tr>
    <tr>
      <td>POST</td>
      <td>/transactions</td>
      <td>Generate a unique transactionId which will be valid for 5 minutes</td>
    </tr>
  </tbody>
</table>
  
Please see for detailed endpoints, requests, responses: 
http://localhost:7002/swagger-ui

### Pre populated data
Some data is populated during boot of the application

#### Accounts
<table>
<thead>
<tr>
  <th>Account Id</th>
  <th>Balance</th>
  <th>Name</th>
  <th>Currency</th>
</tr>
</thead>
<tbody>
<tr>
  <td>0</td>
  <td>10000</td>
  <td>egemen</td>
  <td>USD</td>
</tr>
<tr>
  <td>1</td>
  <td>2500</td>
  <td>jack</td>
  <td>USD</td>
</tr>
<tr>
  <td>2</td>
  <td>30000</td>
  <td>lisa</td>
  <td>EUR</td>
</tr>
</tbody>
</table>

#### Transactions
<table>
<thead>
<tr>
  <th>Id</th>
  <th>Tx Id</th>
  <th>Type</th>
  <th>Related Entity Id</th>
  <th>Amount</th>
  <th>Currency</th>
  <th>Timestamp in MS</th>
</tr>
</thead>
<tbody>
<tr>
  <td>0</td>
  <td>tx-1579123792281-1</td>
  <td>DEPOSIT</td>
  <td>0</td>
  <td>10000</td>
  <td>USD</td>
  <td>1579123792281</td>
</tr>
<tr>
  <td>1</td>
  <td>tx-1579123829349-2</td>
  <td>DEPOSIT</td>
  <td>1</td>
  <td>25000</td>
  <td>USD</td>
  <td>1579123829349</td>
</tr>
<tr>
  <td>2</td>
  <td>tx-1579123845313-3</td>
  <td>DEPOSIT</td>
  <td>2</td>
  <td>30000</td>
  <td>EUR</td>
  <td>1579123845313</td>
</tr>
</tbody>
</table>