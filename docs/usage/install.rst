============
Installation
============

To use Carpet, you must first have the Carpet patches. You can either
build the patches as detailed in the ``README.md`` file at the root of the
git repository, or download them from the
`releases page <https://github.com/gnembon/carpetmod/releases>`_.

Patching automatically
======================

For Linux, a ``mktest.sh`` script is provided in the Carpet git
repository. Simply clone the repository or just download the script
and run it. Run ``./mktest.sh -h`` for help.

A simple recipe for its usage would be to copy the latest Carpet patch
download URL, and run ``./mktest.sh -d <carpet patch url> -o carpet.jar``.

For Windows, a ``mktest.cmd`` script is provided, but it currently lacks
the functionality of ``mktest.sh``. It is intended for building patches
after doing development locally.

Patching manually
=================

Carpet is distributed as a patch for the Minecraft server jar, so it can
be installed by splicing all files from the Carpet archive into the Minecraft
server jar. If this makes sense to you, you don't have to continue reading.

Download
--------

First, create a clean directory (folder). For this tutorial, we call it
``carpet``. There, download the vanilla jar and Carpet patch.

* `Download the 1.12.2 vanilla server jar
  <https://launchermeta.mojang.com/v1/packages/cfd75871c03119093d7c96a6a99f21137d00c855/1.12.2.json>`_.
* `Download the latest Carpet patch
  <https://github.com/gnembon/carpetmod/releases/latest>`_.

.. note::
   For the remainder of this tutorial, we will assume that the server jar is
   called ``minecraft_server.1.12.2.jar``, and the carpet archive is called
   ``carpet-18_06_20.zip``.

Splice
------

Copy the vanilla server jar into a new file. Here, we'll call it
``carpet-1.12.2-18_06_20.jar``.

.. note:: Using a filename like ``carpet-1.12.2-18_06_20.jar`` for your Carpet
          installation allows you to differentiate between server
          brands (vanilla vs. spigot vs. carpet), Minecraft versions, and
          Carpet versions.

Now, copy all the files from the Carpet archive into the Carpet jar, which is
what we called ``carpet-1.12.2-18_06_20.jar``. Jar files are actually just zip
files.

Windows
^^^^^^^

The easiest way to splice the patches into the Carpet jar is:

* Install 7-Zip,
* open both the Carpet jar and Carpet patch archive in 7-Zip, and
* select all files in the opened Carpet archive and drag them into the Carpet
  jar.

You can alternatively follow Gnembon's method in
`his installation video <https://youtu.be/4LKtapbaojs>`_, but be aware that it
is made for an older version.

Linux
^^^^^

In case you are installing the Carpet patches on a Linux machine, you can use
the following commands for our example.

.. code-block:: sh

    # Extract Carpet patches from zip
    mkdir carpet-18_06_20
    cd carpet-18_06_20
    unzip ../carpet-18_06_20.zip

    # Splice into Carpet jar
    jar -uvf ../carpet-1.12.2-18_06_20.jar *

.. note:: The ``jar`` command shown is included in JDK packages. You might
          alternatively use the ``zip`` command, but that is currently
          undocumented.

Running the server
==================

You should now have a proper Carpet installation in the Carpet jar. This is
runnable like any other Java Minecraft server.

See `Sponge's launch script documentation <https://docs.spongepowered.org/latest/en/server/getting-started/launch-script.html>`_.

