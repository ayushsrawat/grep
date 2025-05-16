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

1. [pflag] (https://github.com/spf13/pflag)
2. [color] (https://github.com/fatih/color)

### Ideas

- [x] -v invert flag support
- [ ] -f file name search
- [ ] -x search regex
- [x] -r 1 level of recursive depth
- [ ]-n count occurence of the exact pattern
- [x] -c case sensitive // By default GREP should be in-case-sensitive
