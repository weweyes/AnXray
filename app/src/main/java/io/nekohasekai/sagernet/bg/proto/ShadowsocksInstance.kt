/******************************************************************************
 *                                                                            *
 * Copyright (C) 2021 by nekohasekai <sekai@neko.services>                    *
 * Copyright (C) 2021 by Max Lv <max.c.lv@gmail.com>                          *
 * Copyright (C) 2021 by Mygod Studio <contact-shadowsocks-android@mygod.be>  *
 *                                                                            *
 * This program is free software: you can redistribute it and/or modify       *
 * it under the terms of the GNU General Public License as published by       *
 * the Free Software Foundation, either version 3 of the License, or          *
 *  (at your option) any later version.                                       *
 *                                                                            *
 * This program is distributed in the hope that it will be useful,            *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the              *
 * GNU General Public License for more details.                               *
 *                                                                            *
 * You should have received a copy of the GNU General Public License          *
 * along with this program. If not, see <http://www.gnu.org/licenses/>.       *
 *                                                                            *
 ******************************************************************************/

package io.nekohasekai.sagernet.bg.proto

import cn.hutool.json.JSONObject
import com.github.shadowsocks.plugin.PluginConfiguration
import io.nekohasekai.sagernet.bg.AbstractInstance
import io.nekohasekai.sagernet.fmt.shadowsocks.ShadowsocksBean
import kotlinx.coroutines.CoroutineScope
import libcore.ShadowsocksInstance

class ShadowsocksInstance(val server: ShadowsocksBean, val port: Int) : AbstractInstance {

    lateinit var point: ShadowsocksInstance

    override fun launch() {
        var pluginName = ""
        val pluginOpts = JSONObject()

        if (server.plugin.isNotBlank()) {
            val plugin = PluginConfiguration(server.plugin)
            pluginName = plugin.selected
            val options = plugin.getOptions()
            when (pluginName) {
                "obfs-local" -> {
                    pluginOpts["mode"] = options["obfs"]
                    pluginOpts["host"] = options["obfs-host"]
                }
                "v2ray-plugin" -> {
                    pluginOpts["mode"] = options["mode"]
                    pluginOpts["host"] = options["host"]
                    pluginOpts["path"] = options["path"]

                    if (options.containsKey("tls")) {
                        pluginOpts["tls"] = true
                    }
                    if (options.containsKey("mux")) {
                        pluginOpts["mux"] = true
                    }
                }
            }
        }

        point = ShadowsocksInstance(
            port.toLong(),
            server.finalAddress,
            server.finalPort.toLong(),
            server.password,
            server.method,
            pluginName,
            pluginOpts.toStringPretty()
        )
        point.start()
    }

    override fun destroy(scope: CoroutineScope) {
        if (::point.isInitialized) point.close()
    }

}