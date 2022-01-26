package org.kepler42.testUtils

import org.kepler42.models.Community
import org.kepler42.models.CommunityType
import org.kepler42.models.Contact
import org.kepler42.models.User

fun generateCommunity(name: String, admin: String = "user-id", type: CommunityType = CommunityType.OPEN): Community {
    return Community(
        id = name.hashCode(),
        name = name,
        slug = name.lowercase(),
        admin = admin,
        type = type,
        contacts = listOf(
            Contact(1, title = "Discord", link = "ada#7777"),
            Contact(2, title = "Discord", link = "ada#7777"),
            Contact(3, title = "Discord", link = "ada#7777"),
        )
    )
}

fun generateCommunityWithoutContact(name: String, admin: String = "user-id", type: CommunityType = CommunityType.OPEN): Community {
    return Community(
        id = name.hashCode(),
        name = name,
        slug = name.lowercase(),
        admin = admin,
        type = type,
        contacts = emptyList()
    )
}

fun generateCommunity(name: String): Community {
    return Community(
        id = name.hashCode(),
        name = name,
        slug = name.lowercase(),
        admin = "user-id",
        type = CommunityType.OPEN,
        contacts = emptyList()
    )
}

fun generateUser(name: String) : User {
    return User(
        id = name.hashCode().toString(),
        name = name,
        username = name.lowercase(),
        occupation = "dev",
        photoURL = "https://i.kym-cdn.com/entries/icons/original/000/017/108/faustooooooooooooooooooo.jpg",
        email = "user@gmail.com",
        contact = Contact(0, "Ada#7777", null),
    )
}

fun getAllCommunities(): List<Community> {
    return listOf(
        generateCommunity("Kotlin"),
        generateCommunity("C"),
        generateCommunity("Java"),
        generateCommunity("Counter-String"),
        generateCommunity("Gatos"),
        generateCommunity("Cachorros"),
        generateCommunity("DevOps"),
    )
}

fun getAllUsers(): List<User> {
    return listOf(
        generateUser("Ada"),
        generateUser("Roberto"),
        generateUser("Fausto"),
    )
}
