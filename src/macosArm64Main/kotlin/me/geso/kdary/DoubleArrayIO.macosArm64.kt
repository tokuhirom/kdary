package me.geso.kdary

import okio.FileSystem

internal actual fun getFileSystem(): FileSystem = FileSystem.SYSTEM
