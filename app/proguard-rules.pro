#
# Copyright 2018 Daniel Underhay & Matthew Daley.
#
# This file is part of Walrus.
#
# Walrus is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# Walrus is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with Walrus.  If not, see <http://www.gnu.org/licenses/>.
#

# We're an open source project, so...
-dontobfuscate

# Keep annotations (for devices and OrmLite, etc.)
-keepattributes *Annotation*

# Work around https://sourceforge.net/p/proguard/bugs/531/#e9ed
-keepclassmembers,allowshrinking class android.support.** {
    !static final <fields>;
}

# Parceler library
-keep interface org.parceler.Parcel
-keep @org.parceler.Parcel class * { *; }
-keep class **$$Parcelable { *; }

# Don't warn about these being referenced but not found
-dontwarn com.google.errorprone.**
-dontwarn com.google.gson.**
-dontwarn com.google.j2objc.**
-dontwarn com.sun.jdi.**
-dontwarn java.applet.**
-dontwarn java.lang.**
-dontwarn javax.annotation.**
-dontwarn javax.lang.model.**
-dontwarn javax.persistence.**
-dontwarn javax.servlet.**
-dontwarn javax.tools.**
-dontwarn org.codehaus.mojo.animal_sniffer.**
-dontwarn org.dom4j.**
-dontwarn org.slf4j.**