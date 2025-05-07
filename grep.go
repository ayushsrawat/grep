package main

import (
	"io/fs"
	"log"
	"os"
	"path/filepath"
)

func usage() {
	log.Print("usage: grep [-rnvH] <search-keyword> <where-to-search>")
}

func main() {
	args := os.Args[1:]
	log.SetFlags(log.Flags() &^ (log.Ldate | log.Ltime))
	if len(args) < 1 {
		usage()
		log.Fatal("ERROR: No command is provided")
	}

	// todo: support [-rnvH] & [-r -n -v -H] & [--recusive --new-line --version] commands

	keyword := args[0]
	where := "."
	if len(args) > 1 {
		where = args[1]
	}
	log.Print("Keyword: ", keyword)
	log.Print("Where: ", where)
	filepath.WalkDir(where, func(path string, d fs.DirEntry, err error) error {
		if err != nil {
			return err
		}

		if d.Type().IsRegular() {
			f, err := os.Open(path)
			if err != nil {
				log.Print("Error reading file: ", f.Name())
				return err
			}
			// todo: read this file and regex the searched keyword
		}
		return nil
	})
}
