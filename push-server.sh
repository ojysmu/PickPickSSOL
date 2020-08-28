#!/bin/zsh

cd /Users/ji-hwankim/Workspace/Android/MBTinder/server/build/distributions/Server-dist
rm -rf ./*
tar xf ../server.tar
git add -A
git commit -m "c"
git push
