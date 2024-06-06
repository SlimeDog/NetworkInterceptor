<table border=1><tr><td>
<h2>🛑 End Of Life for SlimeDog/NetworkInterceptor</h2>

SlimeDog/NetworkInterceptor has reached end-of-life

Completed tasks:
- Updated SlimeDog/NetworkInterceptor to support Paper and Spigot 1.20.6 and 1.21.0
- Posted update to SpigotMC
- Updated `Version Support` page of SlimeDog/NetworkInterceptor wiki on Github
- Posted end-of-life notices
  - `Overview` page of SlimeDog/NetworkInterceptor on SpigotMC
  - `README` for SlimeDog/NetworkInterceptor repository on Github
  - `Home` page of SlimeDog/NetworkInterceptor wiki on Github
- Archived SlimeDog/NetworkInterceptor Github repository
- Removed SlimeDog/NetworkInterceptor from bStats
- Removed SlimeDog/NetworkInterceptor from Hangar

Support for future Minecraft versions:
- SlimeDog/NetworkInterceptor will likely work on future Minecraft versions without updates, but there are no guarantees
- Anyone may fork NetworkInterceptor to support future Minecraft versions, or for any other purpose permitted under the GPL3 license
</td></tr></table>

# NetworkInterceptor 

**NetworkInterceptor** detects and (optionally) blocks outgoing network connections. Some examples include:
* Generally required:
  * player authentication (Mojang)
* Generally good:
  * server version check (Minecraft variants: Paper, Spigot, etc.)
  * plugin version check
* Generally not so good:
  * plugin auto-download and install
  * plugin arbitrary code download and execute
  * plugin data reporting

**NetworkInterceptor** installs a custom security manager into the Java runtime environment which logs and (optionally) blocks outgoing network connections.
This allows server administrators easily to monitor the nature of connections made by plugins, and if they desire, prevent them.

### Can it be bypassed?
A plugin with malicious intent could (most likely) find a way to bypass it.

### More detailed analysis
For more detailed analysis of network traffic, we recommend trying [Wireshark](https://www.wireshark.org/).

### Commands
All **NetworkInterceptor** commands are accessible at the console, and in-game with appropriate permissions (default OP). Tab-completion is supported for all commands.

### Version Support
See [Version Support](https://github.com/SlimeDog/NetworkInterceptor/wiki/Version-Support).

### The Wiki
**NetworkInterceptor** is completely documented on The Wiki. Please start there when you have questions.
https://github.com/SlimeDog/NetworkInterceptor/wiki

And we'll never forget: **NetworkInterceptor 1.0** was made with ❤️ by Luck. All credit for the original design and implementation belongs to him.
