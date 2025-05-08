package main

import (
	"bufio"
	"errors"
	"io"
	ifs "io/fs"
	"log"
	"os"
	"path/filepath"
	"regexp"

	flag "github.com/spf13/pflag"
)

func usage(fs *flag.FlagSet) {
	log.Print("usage: grep [-rnvH] <search-keyword> <where-to-search>")
	fs.PrintDefaults()
}

func main() {
	log.SetFlags(log.Flags() &^ (log.Ldate | log.Ltime))

	// todo: support [-rnvH] & [-r -n -v -H] & [--recursive --new-line --version] commands
	fs := flag.NewFlagSet("grep", flag.ContinueOnError)
	fs.SetOutput(os.Stdout)

	help := fs.BoolP("help", "H", false, "Print a brief help message.")
	recursive := fs.BoolP("recursive", "r", false, "Recursively search subdirectories listed.")
	lineNumber := fs.BoolP("line-number", "n", false, "Each output line is preceded by its relative line number in the file, starting at line 1.  The line number counter is reset for each file processed.")

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
	// log.Printf("Keyword: %s", keyword)
	// log.Printf("Where: %s", where)
	if *recursive {
		log.Print("Provided recursive flag")
	}
	if *lineNumber {
		log.Print("Provided line number flag")
	}
	filepath.WalkDir(where, func(path string, d ifs.DirEntry, err error) error {
		if err != nil {
			return err
		}

		if d.Type().IsRegular() {
			f, err := os.Open(path)
			if err != nil {
				log.Printf("Error reading file: %s", f.Name())
				return err
			}
			defer f.Close()
			reader := bufio.NewReader(f)
			searchRegex := `([\w]*)` + regexp.QuoteMeta(keyword) + `([\w]*)`
			reg, err := regexp.Compile(searchRegex)
			if err != nil {
				log.Fatalf("Invalid regex: %v", err)
			}
			lineNumber := 0
			for {
				line, err := reader.ReadString('\n')
				if err != nil {
					if errors.Is(err, io.EOF) {
						break
					}
					if err.Error() == "EOF" {
						break
					}
					log.Fatal("error reading file: ", f)
				}
				lineNumber++

				// todo: https://github.com/fatih/color
				if reg.MatchString(line) {
					log.Printf("%d: %s", lineNumber, line)
				}
			}
		}
		return nil
	})
}
