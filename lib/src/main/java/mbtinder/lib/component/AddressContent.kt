package mbtinder.lib.component

import mbtinder.lib.component.json.JSONParsable
import org.json.JSONObject

class AddressContent: JSONParsable {
    /**
     * 건물명
     */
    private lateinit var bdNm: String

    /**
     * 도로명
     */
    private lateinit var rn: String

    /**
     * 건물번호
     */
    private lateinit var buldMnnm: String

    /**
     * 도로명 주소
     */
    private lateinit var roadAddr: String

    /**
     * 우편번호 (5자리)
     */
    private lateinit var zipNo: String
    fun getZipNumber(): String = zipNo

    /**
     * 광역시,도
     */
    private lateinit var siNm: String
    fun getDoName(): String = siNm
    fun getMetropolitanSiName(): String = siNm

    /**
     * 시,군,구
     */
    private lateinit var sggNm: String
    fun getSiName(): String = sggNm
    fun getGunName(): String = sggNm
    fun getGuName(): String = sggNm

    /**
     * 읍,면,동
     */
    private lateinit var emdNm: String

    fun getEupName(): String = emdNm

    fun getMyeonName(): String = emdNm

    fun getDongName(): String = emdNm

    fun getRoadAddress(): String = roadAddr

    fun getBuildingNumber(): String = buldMnnm

    fun getRoadName(): String = rn

    fun getBuildingName(): String = bdNm

    constructor(buildingName: String, roadName: String, buildingNumber: String, roadAddress: String, zipNumber: String,
                siName: String, guName: String, dongName: String) {
        this.bdNm = buildingName
        this.rn = roadName
        this.buldMnnm = buildingNumber
        this.roadAddr = roadAddress
        this.zipNo = zipNumber
        this.siNm = siName
        this.sggNm = guName
        this.emdNm = dongName

        updateJSONObject()
    }

    constructor(jsonObject: JSONObject): super(jsonObject)
}