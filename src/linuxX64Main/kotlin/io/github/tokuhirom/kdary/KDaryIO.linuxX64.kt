package io.github.tokuhirom.kdary

import okio.FileSystem

internal actual fun getFileSystem(): FileSystem = FileSystem.SYSTEM
