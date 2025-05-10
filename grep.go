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
	"strconv"
	"strings"

	"github.com/fatih/color"
	flag "github.com/spf13/pflag"
)

type cmd struct {
	recursive bool
	showln    bool

	keyword string
	where   string
}

func usage(fs *flag.FlagSet) {
	log.Print("usage: grep [-rnvH] <search-keyword> <where-to-search>")
	fs.PrintDefaults()
}

func main() {
	log.SetFlags(log.Flags() &^ (log.Ldate | log.Ltime))

	// todo: support [-rnvH] & [-r -n -v -H] & [--recursive --new-line --version] commands
	fs := flag.NewFlagSet("grep", flag.ContinueOnError)
	fs.SetOutput(os.Stdout)

	help := fs.BoolP("help", "h", false, "Print a brief help message and exit.")
	recursive := fs.BoolP("recursive", "r", false, "Recursively search subdirectories listed.")
	showln := fs.BoolP("line-number", "n", false, "Each output line is preceded by its relative line number in the file, starting at line 1.  The line number counter is reset for each file processed.")

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
		recursive: *recursive,
		showln:    *showln,
		keyword:   keyword,
		where:     where,
	}

	// todo: read the .hidden files after normal files
	filepath.WalkDir(where, func(path string, d ifs.DirEntry, err error) error {
		if err != nil {
			return err
		}
		if d.IsDir() {
			if *recursive {
				return nil
			}
			return ifs.SkipDir
		}
		return searchInFile(path, c)
	})
}

func searchInFile(file string, cmd cmd) error {
	green := color.New(color.FgGreen).SprintFunc()
	magenta := color.New(color.FgHiMagenta).SprintFunc()
	cyan := color.New(color.FgCyan).SprintFunc()

	f, err := os.Open(file)
	if err != nil {
		log.Printf("Error reading file: %s", f.Name())
		return err
	}
	defer f.Close()
	reader := bufio.NewReader(f)
	searchRegex := `([\w]*)` + regexp.QuoteMeta(cmd.keyword) + `([\w]*)`
	reg, _ := regexp.Compile(searchRegex)

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

		line = strings.TrimRight(line, "\r\n")
		// line = strings.TrimSpace(line)
		if reg.MatchString(line) {
			// todo: highlight the searched text from line
			var sb strings.Builder
			sb.WriteString(green(file))
			if cmd.showln {
				sb.WriteString(":")
				sb.WriteString(magenta(strconv.Itoa(lineNumber)))

			}
			sb.WriteString(": ")
			sb.WriteString(cyan(line))
			log.Print(sb.String())
		}
	}
	return nil
}
