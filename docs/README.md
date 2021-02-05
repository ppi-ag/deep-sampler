# DeepSampler
![Build & Test](https://github.com/ppi-ag/deep-sampler/workflows/Build%20&%20Test/badge.svg) [![Coverage](https://sonarcloud.io/api/project_badges/measure?project=ppi-ag_deep-sampler&metric=coverage)](https://sonarcloud.io/dashboard?id=ppi-ag_deep-sampler) [![Bugs](https://sonarcloud.io/api/project_badges/measure?project=ppi-ag_deep-sampler&metric=bugs)](https://sonarcloud.io/dashboard?id=ppi-ag_deep-sampler) [![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=ppi-ag_deep-sampler&metric=code_smells)](https://sonarcloud.io/dashboard?id=ppi-ag_deep-sampler) [![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=ppi-ag_deep-sampler&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=ppi-ag_deep-sampler) [![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=ppi-ag_deep-sampler&metric=vulnerabilities)](https://sonarcloud.io/dashboard?id=ppi-ag_deep-sampler)

DeepSampler is a __stubbing framework__ for __compound tests__. A _compound_ is a net of objects. A compound can be isolated from an application 
but its content is integrated.  

The API is able to _stub_ methods anywhere _deep_ inside of a compound of any size without the need to manually move a stubbed instance 
from a test case into the compound. 

For tests with large test data (called _Samples_) DeepSampler can separate test logic from test data by _loading_
and _saving_ test data in JSON-files. The JSON-files can be _recorded_ by activating the record-mode and simply running
a test case. If a stub is in record-mode, it routes calls to the original methods and collects all data that flows through the stub. 
This collected data can then be saved to JSON-files.

For light-way tests with smaller Samples, where using separate JSON-files might be unnecessary, DeepSampler 
provides an API that can be used to define Samples
conveniently inside test classes. The API also comes with means to completely redefine the behavior of stubbed methods.


# Choose your environment

DeepSampler is modularized to fit into various environments. The following modules are available by default. 

<!-- tabs:start -->

#### ** JUnit5 & Spring **

![JUnit5](https://junit.org/junit5/assets/img/junit5-logo.png ':size=6%')
![Spring](https://spring.io/images/spring-logo-9146a4d3298760c2e7e49595184e1975.svg ':size=23%')

JUnit 5 Extenstions will be used, and stubbing is done using Spring-AOP

#### ** JUnit5 & Guice **

![JUnit5](https://junit.org/junit5/assets/img/junit5-logo.png ':size=6%')
![Guice](https://lh3.googleusercontent.com/42xsjlsUawN9jo7djAYI_rNXHdABSPHSE-bepeCt9nEn-By9N_U9nf084pryXN0cb5D1QZQtlHuYGnUZgWAkuqzp0kqGUSdhV18eGI4 ':size=13%')


JUnit 5 Extenstions will be used, and stubbing is done using Guice-AOP

#### ** JUnit4 & Spring **

![JUnit4](https://junit.org/junit4/images/junit-logo.png ':size=15%')
![Spring](https://spring.io/images/spring-logo-9146a4d3298760c2e7e49595184e1975.svg ':size=24%')

JUnit 4 Rules will be used, and the stubbing is done using Spring-AOP.

#### ** JUnit4 & Guice **

![JUnit4](https://junit.org/junit4/images/junit-logo.png ':size=15%')
![Guice](https://lh3.googleusercontent.com/42xsjlsUawN9jo7djAYI_rNXHdABSPHSE-bepeCt9nEn-By9N_U9nf084pryXN0cb5D1QZQtlHuYGnUZgWAkuqzp0kqGUSdhV18eGI4 ':size=13%')

JUnit 4 Rules will be used, and the stubbing is done using Guice-AOP.

<!-- tabs:end -->


> The selection of a module-tab is synchronized throughout the complete documentation
 

# License
DeepSampler is made available under the terms of the __MIT License__ (see [LICENSE.md](./LICENSE.md)).

Copyright 2020 PPI AG (Hamburg, Germany)