# mkkdary

## Description

This is a command line tool to create a double array trie file from a plain text file.

Supported format is following:

**key only**

    apple
    banana
    cherry
    date

**key and value**

    apple   100
    banana  200
    cherry  300
    date    400

- key and value is splitted by `\t`.
- value must be an integer.

Every key must be ordered in ascending order.

## How do I run this?

    ./gradlew :cli:mkkdary:run --args "mydict.txt mydata.kdary"
