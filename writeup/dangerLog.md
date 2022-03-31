# 1 Need more Protection From DDoS



We have used `ThreadPool` to do the concurrency control, like to make sure how many threads are allowed during the lauch of application. But chances are good many bad requests are coming and we do not have a list to prevent this from happening.



```java
while(true) {

  Socket client = null;
  try {
    client = this.serverSocket.accept();
    System.out.println("Client accepted");

    threadPool.execute(new Worker(client, this.serverSocket));;
  } catch (Exception e){//whatever bad happens, we close and regard it as bad reqeust
    e.printStackTrace();
  }
}   
```



We read the request by first read the first line, which indicates how many bytes it has.

However, if the client does not send enough bytes, the socket will hang there

* In the future, we can fix this by adding a track timer. If the request has not been received within say 10 seconds, we will reject it

![image-20220331143112286](dangerLog.assets/image-20220331143112286.png)





# 2 Improve Availability

Right now we only have one server being deployed. If this server is down due to physical errors, then we cannot provide the service.



Thus we could have 3-5 servers coordiating using Paxos Protocol

<img src="dangerLog.assets/image-20220331143432437.png" alt="image-20220331143432437" style="zoom:50%;" />

* As a result, if we have 5 servers, we can withstand 2 failures and still function well





# 3 Encryption Action

Right now we have the XML format of request, like 

```xml
<?xml version="1.0" encoding="UTF-8"?>
<create>
	<account id="123456" balance="1000"/>
	<symbol sym="SPY">
		<account id="123458">100000</account>
		<account id="123459">100000</account>
	</symbol>
	<account id="123457" balance="1000"/>
</create>
```



But we could suffer from Man-In-The-Middle Attack where they can acqure what client has asked for.

We could improve on that by adding Encryption Action or Hashing.





















