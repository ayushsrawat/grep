# grep
A custom **[Grep](https://en.m.wikipedia.org/wiki/Grep)-like utility** written in Java that allows searching for keywords in files and directories. This project is currently in development.
## run.sh
```sh
    #!/bin/bash
    
    git clone https://github.com/drsqrt/grep.git
    cd grep
    mvn clean install
    sudo chmod 744 ./bin/run.sh
    ./bin/run.sh -s searchKeyWord -f directoryPath
```
