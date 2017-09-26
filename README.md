# HoldemManager
Tool for analyzing hands and players in PokerStars Texas hold'em. HolderManagers are programs that save all played hands from pokersite logs. Then they give useful information about opponent players during hand based on previously saved hands. 

Our project currently could work as a backend for holdemmanager. It saves all played hands in a way that the information is easily usable.

# Instructions
You need to first create postresql database with HoldemManager/documents/database.sql. Then you can either keep program argument in HoldemManager/history or you can give your own handhistory folder as a argument. By running program adds played hands to database. 

