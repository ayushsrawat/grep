# Grep
A custom **[Grep](https://en.m.wikipedia.org/wiki/Grep)-like utility** written in Go that allows searching for a keyword in files and directories.

## Quick Start
```shell
    git clone https://github.com/ayushsrawat/grep.git &
    cd grep
    go build .
    ./grep [Options] <Keyword> <Where>
```

## Dependencies
1. github.com/spf13/pflag
2. github.com/fatih/color

### Ideas
1. -v invert flag support
2. -f file name search
3. -x search regex
4. -r 1 level of recursive depth
5. -n count occurence of the exact pattern
6. -c case sensitive // By default GREP should be in-case-sensitive