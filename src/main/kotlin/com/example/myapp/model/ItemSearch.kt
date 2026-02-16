package com.example.myapp.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import com.example.myapp.model.ItemSearchResult
import java.sql.DriverManager

@Service
class ItemSearch(
    @Value("\${myapp.db.url}") private val url: String,
    @Value("\${myapp.db.user}") private val user: String,
    @Value("\${myapp.db.pass}") private val pass: String
) {

fun search(keyword: String, synonyms: List<String>): List<ItemSearchResult> {

    // キーワードが空なら空リスト返す（安全）
    if (keyword.isBlank()) return emptyList()

    // キーワード + シノニムをまとめる
    val allKeywords = mutableListOf<String>()
    allKeywords.add(keyword)
    allKeywords.addAll(synonyms)

    // SQL 生成
    val sql = buildString {
        append("SELECT f_name, f_price, f_quantity, f_status FROM t_items WHERE ")
        append(allKeywords.joinToString(" OR ") { "f_name LIKE ?" })
    }

    val results = mutableListOf<ItemSearchResult>()

    DriverManager.getConnection(url, user, pass).use { conn ->
        conn.prepareStatement(sql).use { stmt ->

            // パラメータを正しくセット
            allKeywords.forEachIndexed { index, kw ->
                stmt.setString(index + 1, "%$kw%")
            }

            val rs = stmt.executeQuery()
            while (rs.next()) {
                results.add(
                    ItemSearchResult(
                        name = rs.getString("f_name"),
                        price = rs.getInt("f_price"),
                        quantity = rs.getInt("f_quantity"),
                        status = rs.getInt("f_status")
                    )
                )
            }
        }
    }

    return results
}
}