/*
 *
 *  * Copyright 2020 PPI AG (Hamburg, Germany)
 *  * This program is made available under the terms of the MIT License.
 *
 */

package de.ppi.deepsampler.provider.testservices;


/**
 * A simple Service that implements an interface. The service will be autowired using the interface and is used
 * to test if classes can be stubbed that are decoupled using interfaces, as it will be the default in many applications.
 */
public class DecoupledTestServiceImpl implements DecoupledTestService {

    @Override
    public String sayHello() {
        return "Hello World";
    }
}
