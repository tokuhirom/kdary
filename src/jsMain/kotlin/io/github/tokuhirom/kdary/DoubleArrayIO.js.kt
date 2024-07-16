package io.github.tokuhirom.kdary

import okio.FileSystem
import okio.NodeJsFileSystem

internal actual fun getFileSystem(): FileSystem = NodeJsFileSystem
