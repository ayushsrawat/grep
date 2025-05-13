# Grep
A custom **[Grep](https://en.m.wikipedia.org/wiki/Grep)-like utility** written in Go that allows searching for a keyword in files and directories.

## Quick Start
```shell
    git clone https://github.com/ayushsrawat/grep.git &
    cd grep
    go build .
    ./grep [Options] <Keyword> <Where>
```

### Ideas
1. -v invert flag support
2. -f file name search
3. -x search regex
4. -r 1 level of recursive depth
