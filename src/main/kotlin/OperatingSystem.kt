enum class OS {
    /*WINDOWS,*/ MAC /*, LINUX*/
}

fun getOS(): OS {
    return System.getProperty("os.name")
        .lowercase()
        .let {
            when {
                it.contains("mac") -> OS.MAC
                else -> throw UnsupportedOperationException("Unsupported operating system")
            }
        }
}
