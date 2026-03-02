package com.example.myapp.service.market.shipping

import org.springframework.stereotype.Service

data class Coordinate(
    val lat: Double,
    val lng: Double,
)

@Service
class PrefectureCentroidService {
    fun findByPrefectureId(prefectureId: Int): Coordinate? = PREFECTURE_BY_ID[prefectureId]

    fun findByPrefectureName(prefecture: String): Coordinate? {
        val key = prefecture.trim()
        return PREFECTURE_BY_NAME[key]
    }

    companion object {
        private val PREFECTURE_BY_ID: Map<Int, Coordinate> = mapOf(
            1 to Coordinate(43.064, 141.347),  // 北海道
            2 to Coordinate(40.824, 140.740),  // 青森
            3 to Coordinate(39.703, 141.152),  // 岩手
            4 to Coordinate(38.268, 140.872),  // 宮城
            5 to Coordinate(39.718, 140.103),  // 秋田
            6 to Coordinate(38.240, 140.363),  // 山形
            7 to Coordinate(37.750, 140.467),  // 福島
            8 to Coordinate(36.341, 140.446),  // 茨城
            9 to Coordinate(36.565, 139.883),  // 栃木
            10 to Coordinate(36.391, 139.060), // 群馬
            11 to Coordinate(35.857, 139.649), // 埼玉
            12 to Coordinate(35.605, 140.123), // 千葉
            13 to Coordinate(35.689, 139.692), // 東京
            14 to Coordinate(35.447, 139.642), // 神奈川
            15 to Coordinate(37.902, 139.023), // 新潟
            16 to Coordinate(36.695, 137.211), // 富山
            17 to Coordinate(36.594, 136.626), // 石川
            18 to Coordinate(36.065, 136.221), // 福井
            19 to Coordinate(35.664, 138.568), // 山梨
            20 to Coordinate(36.651, 138.181), // 長野
            21 to Coordinate(35.391, 136.722), // 岐阜
            22 to Coordinate(34.976, 138.383), // 静岡
            23 to Coordinate(35.181, 136.906), // 愛知
            24 to Coordinate(34.730, 136.508), // 三重
            25 to Coordinate(35.004, 135.868), // 滋賀
            26 to Coordinate(35.021, 135.755), // 京都
            27 to Coordinate(34.693, 135.502), // 大阪
            28 to Coordinate(34.691, 135.183), // 兵庫
            29 to Coordinate(34.685, 135.832), // 奈良
            30 to Coordinate(34.226, 135.167), // 和歌山
            31 to Coordinate(35.503, 134.238), // 鳥取
            32 to Coordinate(35.472, 133.051), // 島根
            33 to Coordinate(34.661, 133.935), // 岡山
            34 to Coordinate(34.396, 132.459), // 広島
            35 to Coordinate(34.186, 131.470), // 山口
            36 to Coordinate(34.066, 134.559), // 徳島
            37 to Coordinate(34.341, 134.043), // 香川
            38 to Coordinate(33.841, 132.766), // 愛媛
            39 to Coordinate(33.559, 133.531), // 高知
            40 to Coordinate(33.590, 130.401), // 福岡
            41 to Coordinate(33.249, 130.299), // 佐賀
            42 to Coordinate(32.750, 129.877), // 長崎
            43 to Coordinate(32.803, 130.707), // 熊本
            44 to Coordinate(33.239, 131.609), // 大分
            45 to Coordinate(31.911, 131.424), // 宮崎
            46 to Coordinate(31.560, 130.558), // 鹿児島
            47 to Coordinate(26.212, 127.681), // 沖縄
        )

        private val PREFECTURE_BY_NAME: Map<String, Coordinate> = mapOf(
            "北海道" to PREFECTURE_BY_ID.getValue(1),
            "青森県" to PREFECTURE_BY_ID.getValue(2),
            "岩手県" to PREFECTURE_BY_ID.getValue(3),
            "宮城県" to PREFECTURE_BY_ID.getValue(4),
            "秋田県" to PREFECTURE_BY_ID.getValue(5),
            "山形県" to PREFECTURE_BY_ID.getValue(6),
            "福島県" to PREFECTURE_BY_ID.getValue(7),
            "茨城県" to PREFECTURE_BY_ID.getValue(8),
            "栃木県" to PREFECTURE_BY_ID.getValue(9),
            "群馬県" to PREFECTURE_BY_ID.getValue(10),
            "埼玉県" to PREFECTURE_BY_ID.getValue(11),
            "千葉県" to PREFECTURE_BY_ID.getValue(12),
            "東京都" to PREFECTURE_BY_ID.getValue(13),
            "神奈川県" to PREFECTURE_BY_ID.getValue(14),
            "新潟県" to PREFECTURE_BY_ID.getValue(15),
            "富山県" to PREFECTURE_BY_ID.getValue(16),
            "石川県" to PREFECTURE_BY_ID.getValue(17),
            "福井県" to PREFECTURE_BY_ID.getValue(18),
            "山梨県" to PREFECTURE_BY_ID.getValue(19),
            "長野県" to PREFECTURE_BY_ID.getValue(20),
            "岐阜県" to PREFECTURE_BY_ID.getValue(21),
            "静岡県" to PREFECTURE_BY_ID.getValue(22),
            "愛知県" to PREFECTURE_BY_ID.getValue(23),
            "三重県" to PREFECTURE_BY_ID.getValue(24),
            "滋賀県" to PREFECTURE_BY_ID.getValue(25),
            "京都府" to PREFECTURE_BY_ID.getValue(26),
            "大阪府" to PREFECTURE_BY_ID.getValue(27),
            "兵庫県" to PREFECTURE_BY_ID.getValue(28),
            "奈良県" to PREFECTURE_BY_ID.getValue(29),
            "和歌山県" to PREFECTURE_BY_ID.getValue(30),
            "鳥取県" to PREFECTURE_BY_ID.getValue(31),
            "島根県" to PREFECTURE_BY_ID.getValue(32),
            "岡山県" to PREFECTURE_BY_ID.getValue(33),
            "広島県" to PREFECTURE_BY_ID.getValue(34),
            "山口県" to PREFECTURE_BY_ID.getValue(35),
            "徳島県" to PREFECTURE_BY_ID.getValue(36),
            "香川県" to PREFECTURE_BY_ID.getValue(37),
            "愛媛県" to PREFECTURE_BY_ID.getValue(38),
            "高知県" to PREFECTURE_BY_ID.getValue(39),
            "福岡県" to PREFECTURE_BY_ID.getValue(40),
            "佐賀県" to PREFECTURE_BY_ID.getValue(41),
            "長崎県" to PREFECTURE_BY_ID.getValue(42),
            "熊本県" to PREFECTURE_BY_ID.getValue(43),
            "大分県" to PREFECTURE_BY_ID.getValue(44),
            "宮崎県" to PREFECTURE_BY_ID.getValue(45),
            "鹿児島県" to PREFECTURE_BY_ID.getValue(46),
            "沖縄県" to PREFECTURE_BY_ID.getValue(47),
        )
    }
}
