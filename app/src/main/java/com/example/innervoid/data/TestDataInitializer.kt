package com.example.innervoid.data

import com.example.innervoid.data.models.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.util.UUID

class TestDataInitializer {
    private val db: FirebaseFirestore = Firebase.firestore

    suspend fun initializeTestData() {
        // Создаем тестовых пользователей
//        val users = listOf(
//            User(
//                id = "user1",
//                name = "Иван Иванов",
//                email = "ivan@example.com",
//                deliveryAddress = "ул. Пушкина, д. 10",
//                photoUrl = "https://example.com/photos/user1.jpg"
//            ),
//            User(
//                id = "user2",
//                name = "Мария Петрова",
//                email = "maria@example.com",
//                deliveryAddress = "пр. Ленина, д. 5",
//                photoUrl = "https://example.com/photos/user2.jpg"
//            ),
//            User(
//                id = "admin",
//                name = "Администратор",
//                email = "admin@example.com",
//                deliveryAddress = "ул. Административная, д. 1",
//                photoUrl = "https://example.com/photos/admin.jpg"
//            )
//        )

        // Создаем тестовые продукты
        val products = listOf(
            Product(
                id = "prod1",
                name = "zip hoodie spider heart",
                description = "Стильная худи с принтом паутины и сердца",
                price = 4999.0,
                imageUrl = "https://iimg.su/s/14/yvtBe5imRV6bMoC2HhIjkzIzPuf4OFqCrvs7o80W.jpg",
                category = "Одежда",
                size = "XS, S, M, L, XL",
                inStock = true
            ),
            Product(
                id = "prod2",
                name = "hoodie spider heart",
                description = "Уютная худи с принтом паутины и сердца",
                price = 3400.0,
                imageUrl = "https://iimg.su/s/14/e70hLH0sbe4waZzpDfY3z3Tqh3BsXKM6nmn9w9Fw.jpg",
                category = "Одежда",
                size = "S, M, L, XL",
                inStock = true
            ),
            Product(
                id = "prod3",
                name = "spider sweater",
                description = "Теплый свитер с принтом паутины",
                price = 5500.0,
                imageUrl = "https://iimg.su/s/14/KUadHYffgtVRQQylbdNnvnNQRC56UG818Yb975bJ.jpg",
                category = "Одежда",
                size = "S, M, L, XL",
                inStock = true
            ),
            Product(
                id = "prod4",
                name = "spider jeans",
                description = "Джинсы с принтом паутины",
                price = 5450.0,
                imageUrl = "https://iimg.su/s/14/SBXjjP1vyNC0QAERCo9uV8gq1ogWqHZtdEwcmrML.jpg",
                category = "Одежда",
                size = "S, M, L, XL",
                inStock = true
            ),
            Product(
                id = "prod5",
                name = "gossamer shorts",
                description = "Легкие шорты с принтом паутины",
                price = 2400.0,
                imageUrl = "https://iimg.su/s/14/yN9PR3nqhWcV4KXluUhJIvcIY0VsrtKkxK1JQvlu.jpg",
                category = "Одежда",
                size = "S, M, L, XL",
                inStock = true
            ),
            Product(
                id = "prod6",
                name = "spider heart t-shirt",
                description = "Футболка с принтом паутины и сердца",
                price = 4600.0,
                imageUrl = "https://iimg.su/s/14/63yf0aoDGS1IJs1Nc0mc3PLv7y5QsUv57VHh62oK.jpg",
                category = "Одежда",
                size = "S, M, L, XL",
                inStock = true
            ),
            Product(
                id = "prod7",
                name = "down jacket TNF",
                description = "Теплая куртка The North Face",
                price = 13000.0,
                imageUrl = "https://iimg.su/s/14/08ooGFqfntoNp75Rik1wPjhbzrLjG0XQbKUbwbiC.jpg",
                category = "Одежда",
                size = "S, M, L, XL",
                inStock = true
            ),
            Product(
                id = "prod8",
                name = "anime scarf",
                description = "Шарф с аниме принтом",
                price = 3999.0,
                imageUrl = "https://iimg.su/s/14/T3BFytbh10GtjnwF8pLdmSsT12b07314tdWV1kTG.jpg",
                category = "Аксессуары",
                size = "S, M, L, XL",
                inStock = true
            ),
            Product(
                id = "prod9",
                name = "custom nike sneakers",
                description = "Кастомные кроссовки Nike",
                price = 15000.0,
                imageUrl = "https://iimg.su/s/14/DnSE1KHkPrLjXSZr7154Q6nxmBS183ccvi9wIfv6.jpg",
                category = "Обувь",
                size = "38, 39, 40, 41, 42, 43",
                inStock = true
            )
        )

        // Создаем тестовые элементы корзины
        val cartItems = listOf(
            CartItem(
                id = "cart1",
                userId = "user1",
                productId = "prod1",
                quantity = 2,
                size = "M",
                price = 1999.0
            ),
            CartItem(
                id = "cart2",
                userId = "user1",
                productId = "prod2",
                quantity = 1,
                size = "L",
                price = 4999.0
            )
        )

        // Создаем тестовые сообщения
        val messages = listOf(
            Message(
                id = "msg1",
                senderId = "user1",
                receiverId = "admin",
                content = "Здравствуйте! Когда будет доставка моего заказа?"
            ),
            Message(
                id = "msg2",
                senderId = "admin",
                receiverId = "user1",
                content = "Добрый день! Ваш заказ будет доставлен завтра."
            )
        )

        // Создаем тестовые заказы
        val orders = listOf(
            OrderItem(
                id = "order1",
                userId = "user1",
                productId = "prod1",
                quantity = 2,
                size = "M",
                price = 1999.0
            ),
            OrderItem(
                id = "order2",
                userId = "user2",
                productId = "prod3",
                quantity = 1,
                size = "40",
                price = 5999.0
            )
        )

        // Загружаем данные в Firestore
//        users.forEach { user ->
//            db.collection("users").document(user.id).set(user).await()
//        }

        products.forEach { product ->
            db.collection("products").document(product.id).set(product).await()
        }

        cartItems.forEach { cartItem ->
            db.collection("cart_items").document(cartItem.id).set(cartItem).await()
        }

        messages.forEach { message ->
            db.collection("messages").document(message.id).set(message).await()
        }

        orders.forEach { order ->
            db.collection("orders").document(order.id).set(order).await()
        }
    }
} 