enum class OS {
    /*WINDOWS,*/ MAC, LINUX
}

fun getOS(): OS {
    return System.getProperty("os.name")
        .lowercase()
        .let {
            when {
                it.contains("mac") -> OS.MAC
                it.contains("nix") || it.contains("nux") || it.contains("aix") -> OS.LINUX
                else -> throw UnsupportedOperationException("Unsupported operating system")
            }
        }
}
