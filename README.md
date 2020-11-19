<img src="https://github.com/ppi-ag/deep-sampler/blob/main/docs/assets/logo.svg" alt="DeepSampler" width="33%"/>

![🏗 Build and UnitTests](https://github.com/ppi-ag/deep-sampler/workflows/%F0%9F%8F%97%20Build%20and%20UnitTests/badge.svg) [![Coverage](https://sonarcloud.io/api/project_badges/measure?project=ppi-ag_deep-sampler&metric=coverage)](https://sonarcloud.io/dashboard?id=ppi-ag_deep-sampler) [![Bugs](https://sonarcloud.io/api/project_badges/measure?project=ppi-ag_deep-sampler&metric=bugs)](https://sonarcloud.io/dashboard?id=ppi-ag_deep-sampler) [![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=ppi-ag_deep-sampler&metric=code_smells)](https://sonarcloud.io/dashboard?id=ppi-ag_deep-sampler) [![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=ppi-ag_deep-sampler&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=ppi-ag_deep-sampler) [![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=ppi-ag_deep-sampler&metric=vulnerabilities)](https://sonarcloud.io/dashboard?id=ppi-ag_deep-sampler)

DeepSampler is a stubbing framework for integrated component tests. 

The API is able to _stub_ methods anywhere _deep_ inside of an object tree of any size without the need to manually move a stubbed instance 
from a test case into the object tree. 

For tests with large test data (called _Samples_) DeepSampler can separate test logic from test data by _loading_
and _saving_ test data in JSON-files. The JSON-files can be _recorded_ by activating the record-mode and simply running
a test case. If a stub is in record-mode, it routes calls to the original methods and collects all data that flows through the stub. This collected data can then be saved to JSON-files.

For light-way tests with smaller Samples, where using separate JSON-files might be unnecessary, DeepSampler 
provides an API that can be used to define Samples
conveniently inside test classes. The API also comes with means to freely redefine the behavior of stubbed methods.

## License
DeepSampler is made available under the terms of the __MIT License__ (see [LICENSE.md](./LICENSE.md)).

Copyright 2020 PPI AG (Hamburg, Germany)

