package com.example.myapp.service

import com.example.myapp.model.ItemSearchResult
import java.sql.DriverManager

class ItemSearch(
    private val url: String,
    private val user: String,
    private val pass: String
) {

    fun search(keyword: String, synonyms: List<String>): List<ItemSearchResult> {
        val allKeywords = listOf(keyword) + synonyms

        val sql = buildString {
            append("SELECT f_name, f_price, f_quantity, f_status FROM t_items WHERE ")
            append(allKeywords.joinToString(" OR ") { "f_name LIKE ?" })
        }

        val result = mutableListOf<ItemSearchResult>()

        DriverManager.getConnection(url, user, pass).use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                allKeywords.forEachIndexed { index, k ->
                    stmt.setString(index + 1, "%$k%")
                }

                val rs = stmt.executeQuery()
                while (rs.next()) {
                    result.add(
                        ItemSearchResult(
                            name = rs.getString("f_name"),
                            price = rs.getInt("f_price").takeIf { !rs.wasNull() },
                            quantity = rs.getInt("f_quantity").takeIf { !rs.wasNull() },
                            status = rs.getInt("f_status").takeIf { !rs.wasNull() }
                        )
                    )
                }
            }
        }

        return result
    }
}