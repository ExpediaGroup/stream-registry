# stream-registry

Required Local Environment
---
The local 'dev' version of stream registry requires a running version of Apache Kafka
and Confluent's Stream Registry running locally on port 9092 and 8081 respectively.

A quick version of this is to download, install and start
[Confluent CLI](https://docs.confluent.io/current/cli/index.html).

Build stream-registry
---
```
./stream-registry.sh build
```


Start stream-registry
---
```
./stream-registry.sh run
```

* check that your application is running enter url `http://localhost:8080/swagger`

Run Unit Test , and Jacoco Code Coverage.
---
`mvn clean verify`

The test coverage report is available at `./target/site/jacoco/index.html`

Health Check
---
To see your applications health enter url `http://localhost:8081/healthcheck`

Contributors
---
Special thanks to the following for making stream-registry possible at HomeAway and beyond!

<!-- Contributors START
Adam_Westerman westeras https://www.linkedin.com/in/adam-westerman/ code
Arun_Vasudevan arunvasudevan https://www.linkedin.com/in/arun-vasudevan-55117368/ code
Nathan_Walther nathanwalther https://www.linkedin.com/in/nwalther/ code
Jordan_Moore cricket007 https://www.linkedin.com/in/jordanmoorerhit/ code answers
Carlos_Cordero dccarlos https://www.linkedin.com/in/carlos-d%C3%A1vila-cordero-71128a11b/ code
Ishan_Dikshit ishandikshit https://www.linkedin.com/in/ishan-dikshit-4a1753ba/ code doc
Vinayak_Ponangi vinayakponangi https://www.linkedin.com/in/preethi-vinayak-ponangi-90ba3824/ code talks
Prabhakaran_Thatchinamoorthy prabhakar1983 https://www.linkedin.com/in/prabhakaranthatchinamoorthy/ code
Rui_Zhang ruizhang0519 https://www.linkedin.com/in/rui-zhang-54667a82/ code
Miguel_Lucero mlucero10 https://www.linkedin.com/in/miguellucero/ code answers
Contributors END -->
<!-- Contributors table START -->
| <img src="https://avatars.githubusercontent.com/westeras?s=100" width="100" alt="Adam Westerman" /><br /><sub> Adam Westerman </sub>(https://www.linkedin.com/in/adam-westerman/)<br />[💻](git@github.com:homeaway/stream-registry/commits?author=westeras)                                                    | <img src="https://avatars.githubusercontent.com/arunvasudevan?s=100" width="100" alt="Arun Vasudevan" /><br /><sub> Arun Vasudevan </sub>(https://www.linkedin.com/in/arun-vasudevan-55117368/)<br />[💻](git@github.com:homeaway/stream-registry/commits?author=arunvasudevan) | <img src="https://avatars.githubusercontent.com/nathanwalther?s=100" width="100" alt="Nathan Walther" /><br /><sub> Nathan Walther </sub>(https://www.linkedin.com/in/nwalther/)<br />[💻](git@github.com:homeaway/stream-registry/commits?author=nathanwalther) | <img src="https://avatars.githubusercontent.com/cricket007?s=100" width="100" alt="Jordan Moore" /><br /><sub> Jordan Moore </sub>(https://www.linkedin.com/in/jordanmoorerhit/)<br />[💻](git@github.com:homeaway/stream-registry/commits?author=cricket007) 💁 | <img src="https://avatars.githubusercontent.com/dccarlos?s=100" width="100" alt="Carlos Cordero" /><br /><sub> Carlos Cordero </sub>(https://www.linkedin.com/in/carlos-d%C3%A1vila-cordero-71128a11b/)<br />[💻](git@github.com:homeaway/stream-registry/commits?author=dccarlos) | <img src="https://avatars.githubusercontent.com/ishandikshit?s=100" width="100" alt="Ishan Dikshit" /><br /><sub> Ishan Dikshit </sub>(https://www.linkedin.com/in/ishan-dikshit-4a1753ba/)<br />[💻](git@github.com:homeaway/stream-registry/commits?author=ishandikshit) [📖](git@github.com:homeaway/stream-registry/commits?author=ishandikshit) 
| :---: | :---: | :---: | :---: | :---: | :---: | 
| <img src="https://avatars.githubusercontent.com/prabhakar1983?s=100" width="100" alt="Prabhakaran Thatchinamoorthy" /><br /><sub> Prabhakaran Thatchinamoorthy </sub>(https://www.linkedin.com/in/prabhakaranthatchinamoorthy/)<br />[💻](git@github.com:homeaway/stream-registry/commits?author=prabhakar1983) | <img src="https://avatars.githubusercontent.com/ruizhang0519?s=100" width="100" alt="Rui Zhang" /><br /><sub> Rui Zhang </sub>(https://www.linkedin.com/in/rui-zhang-54667a82/)<br />[💻](git@github.com:homeaway/stream-registry/commits?author=ruizhang0519)                  | <img src="https://avatars.githubusercontent.com/mlucero10?s=100" width="100" alt="Miguel Lucero" /><br /><sub> Miguel Lucero </sub>(https://www.linkedin.com/in/miguellucero/)<br />[💻](git@github.com:homeaway/stream-registry/commits?author=mlucero10) 💁    | <img src="https://avatars.githubusercontent.com/neoword?s=100" width="100" alt="Rene Parra" /><br /><sub> Rene Parra </sub>(https://www.linkedin.com/in/reneparra/)<br />[💻](git@github.com:homeaway/stream-registry/commits?author=neoword) 💁 | <img src="https://avatars.githubusercontent.com/vinayakponangi?s=100" width="100" alt="Vinayak Ponangi" /><br /><sub> Vinayak Ponangi </sub>(https://www.linkedin.com/in/vinayak-ponangi/)<br />[💻](git@github.com:homeaway/stream-registry/commits?author=vinayakponangi) 📢 |
| :---: | :---: | :---: | :---: | :---: |      
<!-- Contributors table END -->
This project follows the [all-contributors](https://github.com/kentcdodds/all-contributors) specification.
