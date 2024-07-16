# mkkdary

## Description

`mkkdary` is a command line tool that creates a double array trie file from a plain text file.

### Supported Formats

You can provide input in two formats:

**Key only**

```
apple
banana
cherry
date
```

**Key and value**

```
apple   100
banana  200
cherry  300
date    400
```

- Keys and values are separated by a tab (`\t`).
- Values must be integers.

You have the option to sort the keys in ascending order using a command-line flag.

## How do I run this?

To run the tool, use the following command:

```sh
./gradlew :cli:mkkdary:run --args "mydict.txt mydata.kdary"
```

### Command-Line Options

- `--sort` : Sort the keys in ascending order before creating the trie.
- `--tab` : Use tab as the separator between keys and values.

Example usage:

```sh
./gradlew :cli:mkkdary:run --args "--sort --tab mydict.txt mydata.kdary"
```

This will read the input from `mydict.txt`, optionally sort the keys, and save the resulting double array trie to `mydata.kdary`.
