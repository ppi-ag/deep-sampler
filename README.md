<img src="/docs/assets/logo.svg?raw=true" alt="DeepSampler" width="40%"/>

Version 2.1.0 - For older versions see [2.0.0](https://github.com/ppi-ag/deep-sampler/tree/%F0%9F%93%9A-maintenance-v2.0.0), [1.1.0](https://github.com/ppi-ag/deep-sampler/tree/%F0%9F%93%9A-maintenance-v1.1.0)

![Build & Test](https://github.com/ppi-ag/deep-sampler/workflows/Build%20&%20Test/badge.svg) [![Coverage](https://sonarcloud.io/api/project_badges/measure?project=ppi-ag_deep-sampler&metric=coverage)](https://sonarcloud.io/dashboard?id=ppi-ag_deep-sampler) [![Bugs](https://sonarcloud.io/api/project_badges/measure?project=ppi-ag_deep-sampler&metric=bugs)](https://sonarcloud.io/dashboard?id=ppi-ag_deep-sampler) [![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=ppi-ag_deep-sampler&metric=code_smells)](https://sonarcloud.io/dashboard?id=ppi-ag_deep-sampler) [![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=ppi-ag_deep-sampler&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=ppi-ag_deep-sampler) [![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=ppi-ag_deep-sampler&metric=vulnerabilities)](https://sonarcloud.io/dashboard?id=ppi-ag_deep-sampler)

# Build integration tests with JUnit and DeepSampler!

DeepSampler is an advanced testing library designed for large software components that require high-quality testing. Unlike traditional unit-testing, DeepSampler's __component-testing__ approach enables you to test several integrated classes within a component, ensuring proper integration while isolating the component from the larger application. With its powerful __features__, DeepSampler simplifies the process of __injecting stubs__ into isolated components and 🎥 __recording 🧪 stub data__ at runtime from real-life examples. 

This library is perfect for developers who need to ensure their software is of the highest quality and is well-suited for a wide range of applications. Give DeepSampler a try and take your testing to the next level!

Let's say, we wanted to test a compound consisting of numerous classes and somewhere deep inside the compound is one class, a DAO, that reads 
data from a Database:

<img src="/docs/assets/deepsampler-demo-unsampled.png?raw=true" alt="A DAO somewhere inside a compound reads data from a database" width="50%"/>

We don't want to access the database during testing. So we mark the methods, that would access the db, as "stubbed" using DeepSampler. If we run the test with DeepSampler in recording-mode, every
call to the marked methods will be intercepted and all data, that was passed to it, or returned by it, is recorded. The recorded data, the __sample__, will be saved to 
a JSON-file.

<img src="/docs/assets/deepsampler-demo-recorder.png?raw=true" alt="All calls to the DAO get intercepted and parameters and return values are recorded" width="50%"/>

As a short appetizer, this is how we tell DeepSampler to attach a stub to the method `load()` in all instances of `MyDao`: 
```
@PrepareSampler
private MyDao myDaoSampler;
...
PersistentSample.of(myDaoSampler.load(Matchers.anyInt()));
```

If we repeat the test with DeepSampler switched to player-mode, the marked method will not be called anymore. Instead, 
a recorded sample from the JSON-file will be returned. 
If the method is called with particular parameters, DeepSampler looks for a sample, that has been recorded with the same 
parameters.

<img src="/docs/assets/deepsampler-demo-player.png?raw=true" alt="Only samples from the previous recording are returned by the stub" width="50%"/>

# 🎓 Examples
We have a second repo where we collect runnable examples with detailed explanations: [DeepSampler Examples 🚀](https://github.com/ppi-ag/deep-sampler-examples).

# Quickstart
The following tutorial demonstrates how to use DeepSampler with JUnit5 and Guice. 

## Installation
We use Maven to build the example. So, as a first step, we add the following dependencies to our pom.xml:

```
<dependency>
   <groupId>de.ppi</groupId>
   <artifactId>deepsampler-junit5</artifactId>
   <version>2.1.0</version>
   <scope>test</scope>
</dependency>

<dependency>
   <groupId>de.ppi</groupId>
   <artifactId>deepsampler-provider-guice</artifactId>
   <version>2.1.0</version>
   <scope>test</scope>
</dependency>

<dependency>
   <groupId>de.ppi</groupId>
   <artifactId>deepsampler-junit-json</artifactId>
   <version>2.1.0</version>
   <scope>test</scope>
</dependency>
```

## The Testee
Let's say, we have a `GreetingService` that would create greeting messages for particular persons. We know only the ID 
of the person we want to greet, so the `GreetingService` needs to lookup the person somewhere (e.g., DB). 
This is done by a 
`PersonService`, which provides a method `getName(personId)`. `getName()` in turn, would use a `PersonDAO` to 
load the `Person` from a Database.  

Now we want to write a test for `GreetingService` and we want `PersonService` to be a part of the tested compound,
so we cannot simply mock `PersonService`. Instead, we want to stub the `PersonDAO` in order to be independent of the
database. 
  
## JUnit Test
We begin by setting up a JUnit Test. In general this will be an ordinary JUnit Test, but we use the 
`DeepSamplerExtension` to activate the DeepSampler environment:

```
@ExtendWith(DeepSamplerExtension.class)
class GreetingServiceTest {
   ...
}
```

 🔎 __Note__ `DeepSamplerExtension` basically provides annotations as a means of convenience for most common
use cases. However, DeepSampler can be used without this extension and even outside of JUnit Tests.
(See [Define a Sampler by API](#define-a-sampler-by-api))

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

Inside a concrete test method we would now use the Sampler to define a stub:
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
                                                    👇
   assertEquals("Hello Sarek!", greetingService.createGreeting(1));
}
```

🔎 __Note__ Just to see that the stub is actually doing something, we could clear all stubs and repeat the
assertion. Now the original method is used again, and we get another return value:

```
void greetingShouldBeGenerated() {
   Sample.of(personDaoSampler.loadPerson(1)).is(new Person("Sarek"));

   assertEquals("Hello Sarek!", greetingService.createGreeting(1));
             👇
   Sampler.clear();
                          👇
   assertEquals("Hello Geordi La Forge!", greetingService.createGreeting(1));
}
```

### Define a Sampler by API
If you want to use DeepSampler outside of JUnit or don't want to use the annotations, it is possible to create a Sampler
using the API:
```
PersonDao personDaoSampler = Sampler.prepare(PersonDao.class);
```

## SamplerFixtures
Usually it will not be enough to write just one test for a particular compound like the `GreetingService`, so it
would be tedious to repeat the definition of stubs in each test case. To ease that, `SamplerFixtures` can be used to 
define reusable stubs. You can also think of a `SampleFixture` as a definition of a test compound, because the stubs
would isolate the compound from the environment in order to make it testable: 
```
public class GreetingServiceCompound implements SamplerFixture {
   @PrepareSampler
   private PersonDao personDaoSampler;

   @Override
   public void defineSamplers() {
      Sample.of(personDaoSampler.loadPerson(Matchers.anyInt())).is(new Person("Sarek"));
   }
}
```
Once we have such a `SamplerFixture`, we can bind it to a test case using the annotation `@UseSamplerFixture` like so:
```
@Test
@UseSamplerFixture(GreetingServiceCompound.class)
void greetingShouldBeGenerated() {
   ...
}
```
The stubs, that are defined by the `SamplerFixture`, are now active for the time the method runs. 

## Persistent Samples
For the sake of understandability, the tested compound in this example is fairly simple. However, DeepSampler was 
especially designed for complex compound tests with quite a lot of stubs which also might return extensive Samples. 
In these cases we would not want
to have big sets of test data (Samples) in JUnit Test classes, we would rather separate test data from test logic. And
possibly more important, we would not want to write such extensive Samples by hand. To ease this, DeepSampler 
can save and load Samples from JSON-files.

### Record a JSON-Sample
<img src="/docs/assets/deepsampler-demo-recorder.png?raw=true" alt="All calls to the DAO get intercepted and parameters and return values are recorded" width="25%" align="left"/>

In order to save Samples in a JSON-file, we __first__ need to define which methods should be stubbed and which methods should be recorded.
This is - again - done using `SamplerFixture`s. In contrast to the example above, we now need to define the Sampler slightly different:
```
     👇                                                              👇               👇
PersistentSample.of(personDaoSampler.loadPerson(PersistentMatchers.anyRecordedInt()))   ;
```

Persistent Samples are defined using `PersistentSample` and we don't need to define a concrete Sample using `is()` anymore, since this value
will be provided by the JSON-File. Additionally, we can now use special parameter matchers for persistent samples. These new matchers honor the fact, that
we now want to match any parameters that have been recorded, instead of explicitlty hard-coded parameter values.
__Second__ we need to tell DeepSampler to record all Data, that flows through the stubbed methods. This is simply done by adding the annotation
`@SaveSamples` to the test method. 

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
`./de/ppi/deepsampler/examples/helloworld/GreetingServiceTest_recordSamplesToJson.json`.

### Load a JSON-Sample

<img src="/docs/assets/deepsampler-demo-player.png?raw=true" alt="Only samples from the previous recording are returned by the stub" width="25%" align="left"/>

Finally, we can use a `SamplerFixture`, and a JSON-file to build a test case. A JSON-file can be loaded using the 
annotation `@LoadSamples`: 

```
@Test
@LoadSamples
@UseSamplerFixture(GreetingServiceCompound.class)
void loadSamplesFromJson() {
   assertEquals("Hello Sarek!", greetingService.createGreeting(1));
}
```
By default `LoadSamples` searches for the JSON-file on the filesystem. The file name is created using the 
full qualified class name, and the method name of the package. In this case DeepSampler would try to load a file named 
`./de/ppi/deepsampler/examples/helloworld/GreetingServiceTest_loadSamplesFromJson.json`.

## Scopes
DeepSampler is by default Thread-scoped. So Samples, that have been defined 
in one Thread, are available only in this particular Thread.

You can change the Scope using `Execution::setScope`. DeepSampler comes with two
predefined Scopes:
   * `ScopeType#THREAD`: Samples are Thread-exclusive, this is the default.
   * `ScopeType#SINGLETON`: The same Samples are available across the entire VM and all Threads share the same Samples.
  
🔎 __Note__ the Scope must be changed before any Samples have been defined.

The following line would make all Samples available across all Threads:
```
    Execution.setScope(ScopeType.SINGLETON);
```

## Authoring custom persistence extensions
The format, in which samplers are recorded, is by default JSON. However, you can change the format by writing your own 
persistence-module. Let's say we wanted to create a persistence layer that is capable of writing YAML. Then we would 
have to implement a model like this:
```mermaid
  graph TD;
      A["MyTest#myTestMethod()"]-- Annotated by -->B["@SaveYamlSamples"]
      B-- Annotated by -->C["@UseSourceManagerForSaving"];
      C-- creates -->D[YamlSourceManagerFactory];
      D-- configures -->E[YamlSourceManager]
      A-- records samples with -->E;
      E-- writes -->F["sample.yaml"];
```
   * `MyTest#myTestMethod()` is an example for a test-method that is supposed to record a sample-file in YAML-format 
      (or any other format, depending on your extension)
   * `@SaveYamlSamples` is a custom annotation that is used as a command to tell DeepSampler, that samples should be 
      recorded. This annotation may be named freely, and it may have all kinds of properties, that can be used by the 
      custom extension to configure the recording.
   * [@UseSourceManagerForSaving](deepsampler-junit/src/main/java/de/ppi/deepsampler/junit/UseSourceManagerForSaving.java) 
     is a meta-annotation that tells DeepSampler, that the annotated annotation is a command for saving samples.
   * `YamlSourceManagerFactory` is referenced by `@UseSourceManagerForsaving` and it is used to create and configure a 
     `SourceManager`. It implements the interface 
     [SourceManagerFactory](deepsampler-junit/src/main/java/de/ppi/deepsampler/junit/SourceManagerFactory.java). 
     The configuration should be done using custom annotations. There is a convenience-method that is able to load 
     annotations from various places, like the test-method itself, or a `SamplerFixture`: 
     [JUnitSamplerUtils#loadAnnotationFromTestOrSamplerFixture](deepsampler-junit/src/main/java/de/ppi/deepsampler/junit/JUnitSamplerUtils.java#L158) 
   * `YamlSourceManager` is the class that is finally able to write the YAML-file. It implements the interface 
      [SourceManager](deepsampler-persistence/src/main/java/de/ppi/deepsampler/persistence/api/SourceManager.java). This 
      class is probably the most complex class to implement, since it needs to translate the 
      [ExecutionInformation](deepsampler-core/src/main/java/de/ppi/deepsampler/core/model/ExecutionInformation.java) to 
      a persistable format. `ExecutionInformation`s contain all data that is collected during the execution of stubbed methods.

Loading samples is implemented very similar. The mentioned interfaces define methods for loading and saving samples. 
The annotation, that commands DeepSampler to load samples, is marked by the meta annotation 
[@UseSourceManagerForLoading](deepsampler-junit/src/main/java/de/ppi/deepsampler/junit/UseSourceManagerForLoading.java)

We recommend placing the extension in two modules:
   1. `persistence-module`: This module contains all code that is related to saving and loading samples independent of 
      JUnit. It should be possible to use the persistence without annotations or JUnit. 
      The [SourceManager](deepsampler-persistence/src/main/java/de/ppi/deepsampler/persistence/api/SourceManager.java)
      -implementation and all it's utilities belong to this module.
   2. `junit-configuration-modul`: A module that contains all code, that is necessary to use the `SourceManager` in 
      JUnit-tests together with annotation-based-configuration. The custom-annotations like `@SaveYamlSamples` and 
      the [SourceManagerFactory](deepsampler-junit/src/main/java/de/ppi/deepsampler/junit/SourceManagerFactory.java)
      -implementation belong to this module.
  

# License
DeepSampler is made available under the terms of the __MIT License__ (see [LICENSE.md](./LICENSE.md)).

Copyright 2022 PPI AG (Hamburg, Germany)

