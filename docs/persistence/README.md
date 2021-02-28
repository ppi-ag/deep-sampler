# Persistence

When cutting compound it will frequently happen, that the size of your compound grows beyond your knowledge. 
Furthermore, in some situations you don't want, or you are not able, to create samples for every method call by hand. If that
happens than the persistence feature of DeepSampler will come to your aid. There are basically two operations the
persistence is made for:
1. Saving method calls (parameter + return value)
2. Loading method calls (as sampled methods)

So the basic idea is that you define the methods you want to save/load the calls from, you execute an integrative testcase and 
make DeepSampler collect all the data which flowed through your defined methods. After that you make DeepSampler load the
data again and as a result you got a full set of stubs. Let's take a look at an example how we could achieve this:
```java
// Step 1. Define you samplers
EmployeeService samplerEmployee = Sampler.prepare(EmployeeService.class);
AccountService samplerAccount = Sampler.prepare(AccountService.class);
FlowService samplerFlow = Sampler.prepare(FlowService.class);

// Step 2. Define the methods you want to record and later load
// with any() we catch every parameter passed in the execution
Sample.of(samplerEmployee.find(any()));
Sample.of(samplerEmployee.search(any(), any()));
Sample.of(samplerAccount.find(any()));
Sample.of(samplerEmployee.read(any()));

// Step 3. Run your testcase in which samples above are used
testee.doLogicWithStubbedServices();

// Step 4. Usage of persistence API for saving the method calls of Step 3.
PersistentSampler.source(JsonSourceManager.builder().buildWithFile("savedMethodCalls.json"))
    .save();

// Step 5. After you saved the method calls in savedMethodCalls.json you can load it again
PersistentSampler.source(JsonSourceManager.builder().buildWithFile("savedMethodCalls.json"))
    .load();

// Step 6. Now you can execute Step 3. again, but this time all methods which were sampled in Step 1. - 2.
// will return the values recorded in Step 4. 
testee.doLogicWithStubbedServices();
```

In a real world scenario would most likely save you test-data once and just run your tests with the saved data. Doing this you can omit
most parts of your integrative test system (for example databases). Furthermore, it enables you to change the data as you like.
For example, you could create situations which you normally wouldn't expect to happen (e.g. missing value in a column of a table when normally this
column should never be null).

Now we got a rough overview about the capabilities of the persistence module. In the following sections we will dive more deeply into
the particular components/steps which were involved in the example.

## Defining samples