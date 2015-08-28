test_home=~/cs455/HW1/src/
     
for i in `cat machine_list`
do
  	echo 'logging into '${i}
    gnome-terminal -x bash -c "ssh -t ${i} 'cd $test_home; java cs455.overlay.node.MessagingNode 129.82.45.77 51975;bash;'" &
done
