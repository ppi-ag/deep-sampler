<img src="https://github.com/ppi-ag/deep-sampler/blob/main/docs/assets/logo.svg" alt="DeepSampler" width="33%"/>

![Build & Test](https://github.com/ppi-ag/deep-sampler/workflows/Build%20&%20Test/badge.svg) [![Coverage](https://sonarcloud.io/api/project_badges/measure?project=ppi-ag_deep-sampler&metric=coverage)](https://sonarcloud.io/dashboard?id=ppi-ag_deep-sampler) [![Bugs](https://sonarcloud.io/api/project_badges/measure?project=ppi-ag_deep-sampler&metric=bugs)](https://sonarcloud.io/dashboard?id=ppi-ag_deep-sampler) [![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=ppi-ag_deep-sampler&metric=code_smells)](https://sonarcloud.io/dashboard?id=ppi-ag_deep-sampler) [![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=ppi-ag_deep-sampler&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=ppi-ag_deep-sampler) [![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=ppi-ag_deep-sampler&metric=vulnerabilities)](https://sonarcloud.io/dashboard?id=ppi-ag_deep-sampler)

DeepSampler is a __stubbing framework__ for __compound tests__. A _compound_ is a net of objects. A compound can be isolated from an application but its content is integrated.  

The API is able to _stub_ methods anywhere _deep_ inside of a compound of any size without the need to manually move a stubbed instance 
from a test case into the compound. 

For tests with large test data (called _Samples_) DeepSampler can separate test logic from test data by _loading_
and _saving_ test data in JSON-files. The JSON-files can be _recorded_ by activating the record-mode and simply running
a test case. If a stub is in record-mode, it routes calls to the original methods and collects all data that flows through the stub. This collected data can then be saved to JSON-files.

For light-way tests with smaller Samples, where using separate JSON-files might be unnecessary, DeepSampler 
provides an API that can be used to define Samples
conveniently inside test classes. The API also comes with means to completely redefine the behavior of stubbed methods.

## 5 minutes tutorial
The following tutorial demonstrates how to use DeepSampler with JUnit5 and Guice. You can download or clone the complete
code from [DeepSampler Examples](https://github.com/ppi-ag/deep-sampler-examples).
### Installation
We will use Maven to build the example. As first step, we add the following dependencies to our pom.xml:

```
<dependency>
   <groupId>de.ppi</groupId>
   <artifactId>deepsampler-junit5</artifactId>
   <version>1.0.1</version>
   <scope>test</scope>
</dependency>

<dependency>
   <groupId>de.ppi</groupId>
   <artifactId>deepsampler-provider-guice</artifactId>
   <version>1.0.1</version>
   <scope>test</scope>
</dependency>
```

Guice and JUnit5 should now also be loaded automatically by Maven, since JUnit5 is a transient dependency of `deepsampler-junit5` and
Guice is a transient dependency of `deepsampler-provider-guice`.

### The Testee
Let's say we have a `GreetingService` that would create greeting messages for particular persons. We know only the ID 
of the person we want to greet, so the `GreetingService` needs to lookup the person somewhere. This is done by a 
`PersonService`, which provides a method `getName(personId)`. `PersonService::getName` in turn, would use a `PersonDAO` to 
load the `Person` from a Database.  

Now we want to write a test for `GreetingService` and we want `PersonService` to be a part of the tested compound,
so we cannot simply mock `PersonService`. Instead, we want to stub the `PersonDAO` in order to be independent of the
database. 
  
### JUnit Test
We begin by setting up a JUnit Test. In general this will be an ordinary JUnit Test, but we use the 
`DeepSamplerExtension` to activate the DeepSampler environment:

```
@ExtendWith(DeepSamplerExtension.class)
class GreetingServiceTest {
   ...
}
```

__[TLDR;]__ `DeepSamplerExtension` basically provides annotations as a means of convenience for most common
use cases of DeepSampler. However, DeepSampler can be used without this extension and even outside of JUnit Tests.
(See [Define a Sampler by API](#Define a Sampler by API))

The actual stubbing is done by an aop-provider that uses - in this case - Guice aop. It is activated by passing
the `DeepSamplerModule` to guice. In real life we would most likely have a Guice module for the testee, and we would want
 to combine the testee's module with `DeepSamplerModule`, however for the sake of simplicity we use 
 `DeepSamplerModule` directly for now, when we tell Guice to inject members into our test class: 
```
@BeforeEach
void injectWithGuice() {
   Guice.createInjector(new DeepSamplerModule()).injectMembers(this);
}
```

### Define a stub
First we need a __Sampler__ which serves as a model, that describes which methods should be stubbed and what the
stub should do. The Sampler is an instance of the class, that we want to stub. We define the Sampler by
adding a property with the type of the stubbed class and add the annotation `PrepareSampler` to it:

```
@ExtendWith(DeepSamplerExtension.class)
class GreetingServiceTest {

   @PrepareSampler
   private PersonDao personDaoSampler;
   ...
}
```

Inside of a concrete test method we would now use the Sampler to define a stub:
```
@Test
void greetingShouldBeGenerated() {
   Sample.of(personDaoSampler.loadPerson(1)).is(new Person("Sarek"));
   ...
}
```
This means, the method `PersonDao::loadPerson` will be stubbed. If `loadPerson` is called with the parameter
`1` the stub will return the __Sample__ `new Person("Sarek")`. Otherwise, the original method will be called.
The stub applies to all instances of `PersonDAO`, no matter where the instances occur. 
It will be active for the lifetime of the test method, so each test method can declare its own stubs. 

That's it, we can now call the testee and check the results as usual:

```
@Test
void greetingShouldBeGenerated() {
   Sample.of(personDaoSampler.loadPerson(1)).is(new Person("Sarek"));

   assertEquals("Hello Sarek!", greetingService.createGreeting(1));
}
```

__[TLDR;]__ Just to see that the stub is actually doing something, we could clear all stubs and repeat the
assertion. Now the original method is used again, and we get another return value:

```
void greetingShouldBeGenerated() {
   Sample.of(personDaoSampler.loadPerson(1)).is(new Person("Sarek"));

   assertEquals("Hello Sarek!", greetingService.createGreeting(1));

   Sampler.clear();

   assertEquals("Hello Geordi La Forge!", greetingService.createGreeting(1));
}
```

### Define a Sampler by API
If you want to use DeepSampler outside of JUnit or don't want to use the annotations, it is possible to create a Sampler
using the API:
```
PersonDao personDaoSampler = Sampler.prepare(PersonDao.class);
```

### SamplerFixtures
Usually it will not be enough to write just one test for a particular compound like the `GreetingService`, so it
would be tedious to repeat the definition of stubs in each test case. To ease that, `SamplerFixtures` can be used to 
define reusable stubs. You can also think of a SampleFixutre as a definition of a test compound, because the stubs
would isolate the compound from the environment in order to make it testable: 
```
public class GreetingServiceCompound implements SamplerFixture {
   @PrepareSampler
   private PersonDao personDaoSampler;

   @Override
   public void defineSamplers() {
      Sample.of(personDaoSampler.loadPerson(Matchers.anyInt()));
   }
}
```
Once we have such a SamplerFixture, we can bind it to a test case using the annotation `@UseSamplerFixture` like so:
```
@Test
@UseSamplerFixture(GreetingServiceCompound.class)
void recordSamplesToJson() {
   ...
}
```
The stubs, that are defined by the SamplerFixture, are now active for the time the method runs. 

You might have noticed that the stub doesn't define a concrete Sample anymore. We have done this, because we
rather want to read the Samples from a JSON-file now. How this can be achieved is explained by the next two chapters.

### Record a JSON-Sample
For the sake of understandability, the tested compound in this example is fairly simple, however DeepSampler was 
especially designed for complex testees with quite a lot of stubs which also might return extensive Samples. 
In these cases we would not want
to have big sets of test data (Samples) in JUnit Test classes, we would rather separate test data from test logic. This
can be done by saving and loading Samples from JSON-files.

In order to do that, we simply add the annotation 
`@SaveSamples` to our method and call the method that, we want to test. The methods that should be stubbed are 
defined by the SamplerFixture that we have introduced in the preceding chapter. 

```
@Test
@SaveSamples
@UseSamplerFixture(GreetingServiceCompound.class)
void recordSamplesToJson() {
   greetingService.createGreeting(1);
}
```
When we run this test, the stubs will call the original methods and additionally record the parameters and return values
for each call. The recorded data is then saved to a JSON-file.

By default `@SaveSamples` saves the JSON-file in a folder corresponding to the package of the current test case. The 
filename is created using the class name, and the method name of the package. In this case we would get a file named
`de/ppi/deepsampler/examples/helloworld/GreetingServiceTest_recordSamplesToJson.json`.

### Load a JSON-Sample
Finally, we can use a SamplerFixture, and a JSON-file to build a test case. A JSON-file can be loaded using the 
annotation `@LoadSamples`: 

```
@Test
@LoadSamples()
@UseSamplerFixture(GreetingServiceCompound.class)
void loadSamplesFromJson() {
   assertEquals("Hello Sarek!", greetingService.createGreeting(1));
}
```
By default `LoadSamples` searches for the JSON-file on the classpath. It expects the JSON-file in the same package
where the current test case is located. The file name is created using the class name, and the method name of the package. 
In this case DeepSampler would try to load a file named 
`de/ppi/deepsampler/examples/helloworld/GreetingServiceTest_loadSamplesFromJson.json`.


## License
DeepSampler is made available under the terms of the __MIT License__ (see [LICENSE.md](./LICENSE.md)).

Copyright 2020 PPI AG (Hamburg, Germany)

