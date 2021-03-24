package ru.skillbranch.kotlinexample

import androidx.annotation.VisibleForTesting

object UserHolder {
    private val map = mutableMapOf<String, User>()

    //TODO проверка того,есть ли данный email/phone уже в map
    fun registerUser(
        fullName: String,
        email: String,
        password: String
    ): User = User.makeUser(fullName, email = email, password = password)
        .also { user ->
            checkUserExist(user, "email")
            map[user.login] = user
        }

    fun registerUserByPhone(
        fullName: String,
        rawPhone: String
    ): User = User.makeUser(fullName, phone = rawPhone)
        .also { user ->
            checkUserExist(user, "phone")
            map[user.login] = user
        }

    fun loginUser(login: String, password: String): String? {
        val _login = login.replace("""[^+\d]""".toRegex(), "")
        var user: User?
        user = if (_login.matches("""^\+\d{11}$""".toRegex())) {
            map[_login]
        } else {
            map[login.trim()]
        }
        return user?.let {
            if (it.checkPassword(password)) it.userInfo
            else null
        }
    }

    fun requestAccessCode(login: String) {
        val _login = login.replace("""[^+\d]""".toRegex(), "")
        if (_login.matches("""^\+\d{11}$""".toRegex())) { map[_login.trim()]?.newAccessCode() }
        else { throw java.lang.IllegalArgumentException("Enter a valid phone number starting with a + and containing 11 digits") }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun clearHolder() {
        map.clear()
    }

    fun importUsers(list: List<String>): List<User> {
        var userList: List<User> = emptyList()

        for (item in list) {
            val data: List<String> = item.split("""[;|:]""".toRegex())

            val user = User.loadUser(data[0], data[1], data[2], data[3], data[4])

            map[user.login] = user
            userList.plusElement(user)
        }

        return userList
    }

    private fun checkUserExist(user: User, login: String = "login"): Boolean = map.contains(user.login)
        .also { if (it) throw IllegalArgumentException("A user with this $login already exists") }

}