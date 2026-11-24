package com.ivy.subscription

import androidx.compose.runtime.Immutable
import java.util.Locale

@Immutable
data class CountryPhoneCode(
    val flag: String,
    val name: String,
    val iso: String,
    val dialCode: String,
    val placeholder: String
)

val CountryPhoneCodes: List<CountryPhoneCode> = listOf(
    CountryPhoneCode("🇵🇰", "Pakistan", "PK", "+92", "300 1234567"),
    CountryPhoneCode("🇺🇸", "United States", "US", "+1", "202 555 0143"),
    CountryPhoneCode("🇬🇧", "United Kingdom", "GB", "+44", "7700 900123"),
    CountryPhoneCode("🇦🇪", "United Arab Emirates", "AE", "+971", "50 123 4567"),
    CountryPhoneCode("🇸🇦", "Saudi Arabia", "SA", "+966", "50 123 4567"),
    CountryPhoneCode("🇮🇳", "India", "IN", "+91", "98765 43210"),
    CountryPhoneCode("🇨🇦", "Canada", "CA", "+1", "416 555 0199"),
    CountryPhoneCode("🇦🇺", "Australia", "AU", "+61", "412 345 678"),
    CountryPhoneCode("🇩🇪", "Germany", "DE", "+49", "151 23456789"),
    CountryPhoneCode("🇫🇷", "France", "FR", "+33", "6 12 34 56 78"),
    CountryPhoneCode("🇳🇱", "Netherlands", "NL", "+31", "6 12345678"),
    CountryPhoneCode("🇪🇸", "Spain", "ES", "+34", "612 34 56 78"),
    CountryPhoneCode("🇮🇹", "Italy", "IT", "+39", "312 345 6789"),
    CountryPhoneCode("🇹🇷", "Turkey", "TR", "+90", "501 234 5678"),
    CountryPhoneCode("🇧🇩", "Bangladesh", "BD", "+880", "1712 345678"),
    CountryPhoneCode("🇮🇩", "Indonesia", "ID", "+62", "812 3456 7890"),
    CountryPhoneCode("🇲🇾", "Malaysia", "MY", "+60", "12 345 6789"),
    CountryPhoneCode("🇸🇬", "Singapore", "SG", "+65", "8123 4567"),
    CountryPhoneCode("🇵🇭", "Philippines", "PH", "+63", "917 123 4567"),
    CountryPhoneCode("🇪🇬", "Egypt", "EG", "+20", "100 123 4567"),
    CountryPhoneCode("🇳🇬", "Nigeria", "NG", "+234", "802 123 4567"),
    CountryPhoneCode("🇿🇦", "South Africa", "ZA", "+27", "82 123 4567"),
    CountryPhoneCode("🇧🇷", "Brazil", "BR", "+55", "11 91234 5678"),
    CountryPhoneCode("🇲🇽", "Mexico", "MX", "+52", "55 1234 5678"),
    CountryPhoneCode("🇯🇵", "Japan", "JP", "+81", "90 1234 5678"),
    CountryPhoneCode("🇰🇷", "South Korea", "KR", "+82", "10 1234 5678"),
    CountryPhoneCode("🇧🇬", "Bulgaria", "BG", "+359", "88 123 4567"),
    CountryPhoneCode("🇵🇱", "Poland", "PL", "+48", "501 234 567"),
    CountryPhoneCode("🇶🇦", "Qatar", "QA", "+974", "3312 3456"),
    CountryPhoneCode("🇰🇼", "Kuwait", "KW", "+965", "5001 2345"),
    CountryPhoneCode("🇴🇲", "Oman", "OM", "+968", "9212 3456"),
    CountryPhoneCode("🇧🇭", "Bahrain", "BH", "+973", "3600 1234"),
    CountryPhoneCode("🇮🇪", "Ireland", "IE", "+353", "87 123 4567"),
    CountryPhoneCode("🇨🇭", "Switzerland", "CH", "+41", "79 123 45 67"),
    CountryPhoneCode("🇸🇪", "Sweden", "SE", "+46", "70 123 45 67"),
    CountryPhoneCode("🇳🇿", "New Zealand", "NZ", "+64", "21 123 4567")
)

fun defaultCountryForLocale(locale: Locale = Locale.getDefault()): CountryPhoneCode {
    val iso = locale.country.uppercase(Locale.ROOT)
    return CountryPhoneCodes.firstOrNull { it.iso == iso } ?: CountryPhoneCodes.first()
}

fun splitE164Phone(
    rawPhone: String,
    fallback: CountryPhoneCode
): Pair<CountryPhoneCode, String> {
    val cleaned = rawPhone.trim().replace(Regex("[^+0-9]"), "")
    if (cleaned.isBlank()) return fallback to ""
    val normalized = if (cleaned.startsWith("+")) cleaned else "+$cleaned"
    val match = CountryPhoneCodes
        .sortedByDescending { it.dialCode.length }
        .firstOrNull { normalized.startsWith(it.dialCode) }
    return if (match != null) {
        match to normalized.removePrefix(match.dialCode)
    } else {
        fallback to normalized.removePrefix("+")
    }
}

fun buildE164Phone(
    country: CountryPhoneCode,
    localDigits: String
): String {
    val digitsOnly = localDigits.filter { it.isDigit() }.trimStart('0')
    return "${country.dialCode}$digitsOnly"
}
