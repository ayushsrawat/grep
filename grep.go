package main

import (
	"bufio"
	"fmt"
	ifs "io/fs"
	"log"
	"os"
	"path/filepath"
	"regexp"
	"strconv"
	"strings"

	"github.com/fatih/color"
	flag "github.com/spf13/pflag"
)

type cmd struct {
	recursive     bool
	showln        bool
	caseSensitive bool
	keyword       string
	where         string
}

var (
	green   = color.New(color.FgGreen).SprintFunc()
	magenta = color.New(color.FgHiMagenta).SprintFunc()
	red     = color.New(color.FgRed).SprintFunc()
)

func usage(fs *flag.FlagSet) {
	log.Print("usage: grep [-rnvH] <search-keyword> <where-to-search>")
	fs.PrintDefaults()
}

func main() {
	log.SetFlags(log.Flags() &^ (log.Ldate | log.Ltime))

	// todo: support [-rnvH] & [-r -n -v -h -w] & [--recursive --new-line --invertmatch] commands
	fs := flag.NewFlagSet("grep", flag.ContinueOnError)
	fs.SetOutput(os.Stdout)

	help := fs.BoolP("help", "h", false, "Print a brief help message and exit.")
	recursive := fs.BoolP("recursive", "r", false, "Recursively search subdirectories listed.")
	showln := fs.BoolP("line-number", "n", false, "Each output line is preceded by its relative line number in the file, starting at line 1.  The line number counter is reset for each file processed.")
	caseSensitive := fs.BoolP("case-sensitive", "c", false, "Perform case sensitive matching. By default, grep is case insensitive.")

	if err := fs.Parse(os.Args[1:]); err != nil {
		usage(fs)
		os.Exit(1)
	}
	args := fs.Args()
	if len(args) < 1 || *help {
		usage(fs)
		os.Exit(0)
	}

	keyword := args[0]
	where := "."
	if len(args) > 1 {
		where = args[1]
	}

	c := cmd{
		recursive:     *recursive,
		showln:        *showln,
		keyword:       keyword,
		where:         where,
		caseSensitive: *caseSensitive,
	}

	// todo: read the .hidden files after normal files
	err := filepath.WalkDir(where, func(path string, d ifs.DirEntry, err error) error {
		if err != nil {
			return err
		}
		if d.IsDir() {
			if *recursive {
				return nil
			}
			// fixme: skipping level 0 dir when -r not provided?
			return ifs.SkipDir
		}
		return searchInFile(path, c)
	})
	if err != nil {
		log.Fatalf("error traversing path: %v", err)
	}
}

func searchInFile(file string, cmd cmd) error {
	f, err := os.Open(file)
	if err != nil {
		log.Printf("Error reading file: %s", f.Name())
		return err
	}
	defer f.Close()

	searchRegex := regexp.QuoteMeta(cmd.keyword)
	// searchRegex := `([\w]*)` + regexp.QuoteMeta(cmd.keyword) + `([\w]*)`
	ignoreCase := `(?i)`
	if cmd.caseSensitive {
		ignoreCase = ""
	}
	reg, err := regexp.Compile(ignoreCase + searchRegex)
	if err != nil {
		log.Fatalf("invalid regex: %v", err)
	}

	scanner := bufio.NewScanner(f)
	lineNumber := 0
	for scanner.Scan() {
		lineNumber++
		line := scanner.Text()
		line = strings.TrimRight(line, "\r\n")
		// line = strings.TrimSpace(line)
		matches := reg.FindAllStringIndex(line, -1)
		if len(matches) > 0 {
			var sb strings.Builder
			sb.WriteString(green(file))
			if cmd.showln {
				sb.WriteString(":")
				sb.WriteString(magenta(strconv.Itoa(lineNumber)))
			}
			sb.WriteString(": ")
			last := 0
			for _, m := range matches {
				sb.WriteString(line[last:m[0]])
				sb.WriteString(red(line[m[0]:m[1]]))
				last = m[1]
			}
			sb.WriteString(line[last:])
			log.Print(sb.String())
		}
	}
	if err := scanner.Err(); err != nil {
		return fmt.Errorf("error scanning file %s: %w", file, err)
	}
	return nil
}
