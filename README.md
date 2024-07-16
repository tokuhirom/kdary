# KDary

KDary is a Double Array Trie (DAT) library for Kotlin, providing efficient common prefix searches. This library is designed to facilitate Natural Language Processing (NLP) tasks such as developing Input Method Editors (IMEs) and morphological analyzers using Kotlin Multiplatform.

The core logic is ported from [darts-clone](https://github.com/s-yata/darts-clone/), originally implemented in C++.

## Features

- High-speed common prefix search.
- Suitable for various NLP tasks such as:
  - Developing Input Method Editors (IMEs)
  - Implementing morphological analyzers
  - Building predictive text engines
  - Creating autocomplete systems
  - Performing keyword search

## Supported Environments

- Kotlin 2.0.0 or later
- One of the following environments:
  - JVM 17.0 or later
  - JS
  - Linux (x64)
  - macOS (Arm, x64)

## Installation

Add the following dependency to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("me.geso.kdary:kdary:1.0.0")
}
```

## Usage

### Creating a Double Array Trie

```kotlin
import io.github.tokuhirom.kdary.KDary

val dictionary = listOf("apple", "app", "application", "apply")
val dat = KDary.build(dictionary)
```

### Performing Prefix Search

```kotlin
val prefix = "app"
val results = dat.commonPrefixSearch(prefix)
println("Common prefixes for '$prefix': $results")
// Output: Common prefixes for 'app': [app, apple, application, apply]
```

### Exact Match Search

```kotlin
val word = "apple"
val exactMatch = dat.exactMatchSearch(word)
println("Exact match for '$word': $exactMatch")
// Output: Exact match for 'apple': true
```

### Traversing the Trie

```kotlin
val traverseResults = dat.traverse("app") { prefix, word ->
    println("Traversed word: $word with prefix: $prefix")
}
```

### Saving and Loading the Trie

```kotlin
import io.github.tokuhirom.kdary.saveKDary
import io.github.tokuhirom.kdary.loadKDary

// Save to a file
saveKDary(dat, "dat.trie")

// Load from a file
val loadedDat = loadKDary("dat.trie")
```

## API Documentation

### KDary

#### Constructor

`KDary.build(words: List<String>): KDary`

Creates a Double Array Trie from the given list of words.

#### Methods

- `fun commonPrefixSearch(query: String): List<String>`

  Returns a list of words that are common prefixes of the given query.

- `fun exactMatchSearch(word: String): Boolean`

  Returns `true` if the exact word exists in the trie, `false` otherwise.

- `fun traverse(prefix: String, action: (String, String) -> Unit)`

  Traverses the trie starting from the given prefix, applying the provided action to each word found.

### KDaryIO

#### Methods

- `fun saveKDary(kdary: KDary, fileName: String)`

  Saves the given `KDary` instance to the specified file path.

- `fun loadKDary(fileName: String): KDary`

  Loads a `KDary` instance from the specified file path.

## License

The ported version of this library, written in Kotlin, is distributed under the MIT License as described below.

```
The MIT License (MIT)

Copyright © 2024 Tokuhiro Matsuno, http://64p.org/ <tokuhirom@gmail.com>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the “Software”), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO, THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
```

This library is a port of the original [darts-clone](https://github.com/s-yata/darts-clone), which was originally written in C++. The original darts-clone is distributed under the BSD 2-clause license, as described below.

```
# The BSD 2-clause license

Copyright (c) 2008-2014, Susumu Yata
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
* Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
```

## Thanks to

Special thanks to Susumu Yata for the original [darts-clone](https://github.com/s-yata/darts-clone) implementation.
