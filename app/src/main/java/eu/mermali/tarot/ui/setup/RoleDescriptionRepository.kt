package eu.mermali.tarot.ui.setup

import android.content.Context

data class RoleDescription(val id: String, val name: String, val description: String)

class RoleDescriptionRepository(private val context: Context) {
    fun load(): Map<String, RoleDescription> {
        return runCatching {context.assets.open(RoleDescriptionsAsset).bufferedReader().use { reader -> parseRoleDescriptions(reader.readText()) }}.getOrDefault(emptyMap())
    }

    private fun parseRoleDescriptions(rawYaml: String): Map<String, RoleDescription> {
        val roles = mutableListOf<RoleDescription>()
        var id: String? = null
        var name: String? = null
        var description: String? = null

        fun flushRole() {
            val roleId = id
            if (!roleId.isNullOrBlank()) { roles += RoleDescription(id = roleId, name = name.orEmpty().ifBlank { roleId }, description = description.orEmpty()) }
            id = null
            name = null
            description = null
        }

        rawYaml.lineSequence().forEach { rawLine ->
            val line = rawLine.trim()
            if (line.isBlank() || line.startsWith("#") || line == "roles:") { return@forEach }

            if (line.startsWith("- ")) {
                flushRole()
                val firstField = line.removePrefix("- ").trim()
                if (firstField.startsWith("id:")) { id = firstField.yamlValue() }
                return@forEach
            }

            when {
                line.startsWith("id:") -> id = line.yamlValue()
                line.startsWith("name:") -> name = line.yamlValue()
                line.startsWith("description:") -> description = line.yamlValue()
            }
        }
        flushRole()
        return roles.associateBy { it.id }
    }

    private fun String.yamlValue(): String {
        return substringAfter(':').trim().removeSurrounding("\"").removeSurrounding("'")
    }

    private companion object { const val RoleDescriptionsAsset = "role_descriptions.yaml" }
}
