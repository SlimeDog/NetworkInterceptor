### NetworkInterceptor
Monitor and block outgoing network requests

NetworkInterceptor 1.0 was made with <3 by Luck. All credit for the original design and implementation belong to him

### Overview
NetworkInterceptor detects and (optionally) blocks outgoing network connections. Some examples include:
* automatic updates
* constant nagging to update to the latest version
* download and execute arbitrary code at runtime from unknown or untrustworthy sources
* plugins which report to unknown network addresses without the option to opt-out

* Generally good:
  * player authentication (Mojang)
  * server version check (Minecraft variants: Paper, Spigot, etc.)
  * plugin version check
* Generally not so good:
  * plugin auto-download and install
  * plugin arbitrary code download and execute
  * plugin data reporting

NetworkInterceptor installs a custom security manager into the Java runtime environment which logs and (optionally) blocks outgoing network connections.
It lets server administrators easily monitor the nature of connections made by plugins, and if they desire, prevent them.

### Can it be bypassed?
A plugin with malicious intent could (most likely) find a way to bypass it.

### More detailed analysis
For more detailed analysis of network traffic, we recommend trying [Wireshark](https://www.wireshark.org/).

### Commands
All NetworkInterceptor commands are accessible at the console, and in-game with appropriate permissions (default OP). Tab-completion is supported for all commands.

### Message localization
All NetworkInterceptor messages may be localized. Message changes take effect on plugin reload or server restart.

### Version Support
NetworkInterceptor 1.0.0 was certified for Spigot 1.12.2, 1.13.2, 1.14.4, 1.15.2, and 1.16.5.
NetworkInterceptor 2.0.0 is certified for Spigot and Paper 1.17.x.

NetworkInterceptor may or may not work on previous Spigot releases or other non-Spigot variants.

Note: Only certified releases are supported.

### The Wiki
NetworkInterceptor is completely documented on The Wiki. Please start there when you have questions.
https://github.com/SlimeDog/NetworkInterceptor/wiki
