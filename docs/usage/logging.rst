======================
Monitoring game events
======================

To keep tabs on internal game values, you can use Carpet loggers.
When you *subscribe* to a logger, Carpet displays and automatically
updates its information at the bottom of the TAB menu.

Using the interactive menu
==========================

Carpet provides an interactive logging menu, accessible with the ``/log``
command. Simply entering the command will display all loggers and their
current status.
Some loggers have different modes visible in the ``/log`` menu.
Click on a mode to subscribe to it. If a logger is activated, click the
red ``[X]`` to unsubscribe.

Using the command interface
===========================

Use ``/log [logger] [mode]`` to subscribe to and unsubscribe from
Carpet loggers. If the mode is unspecified, or an invalid mode, the
command will toggle subscription for the given logger, using the default
mode if subscribing.

To unsubscribe from all loggers, use the ``/log clear`` command.

