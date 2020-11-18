![DeepSamplerA](./docs/assets/logo.svg)
DeepSampler is a stubbing framework for integrated component tests. 

The API is able to stub
methods anywhere _deep_ inside of an object tree of any size without the need to manually move a stubbed instance 
from a test case into the object tree. 

In order to cope with large test data (called _Samples_) DeepSampler can separate test logic from test data by loading
and saving test data in JSON-files. Stubs can be switched to a record mode in order to record test data by running 
a test case using the original (stubbed) data sources. 

For light-way tests with smaller Samples, where using separate JSON-files might be unnecessary, DeepSampler 
provides an API that can be used to define Samples
conventionally inside test classes. The API also comes with means to freely redefine the behavior of stubbed methods,
for situations where static Samples are insufficient. 

## License
DeepSampler is made available under the terms of the __MIT License__ (see [LICENSE.md](./LICENSE.md)).

Copyright 2020 PPI AG (Hamburg, Germany)

