package est.tunzo.cyberpros.server

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform