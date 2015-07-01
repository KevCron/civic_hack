# civic_hack
Civic app

Civic app

An app I made for the Indy Civic Hack. The challenge was to create something that would streamline the way the Indianapolis Public School district provides gas reimbursements to contractors.

The product my team (Kevin Cronly[Android], Chris McDonald[NodeJS backend], Harvey Kadyanji[Web App]) made included a contractor facing native Android app, a backend API written in NodeJS, and a manager/accountant web portal for seeing the claims and providing analytics, using BootStrap.

The bulk of the project was made in only 7 hours.

The app allows users to login, then begin a trip with the push of a button. The app tracks the users location, drawing his or her path on a Google Map fragment as they go, until the user presses 'End Trip.' The app then allows users to fill out relevant info about their trip (Reason of business, notes, car driven etc) and submit a reimbursement claim. The app tracks the distance traveled by the user, user start and user stop location, and includes this in the submitted claim.

The app UI was all Material Design, using the recently released Android Design Support Libraries.

The app then makes a POST request to our server, where the backend API sends it and additional analytics to the manager portal.

Our team won the Indy Civic City Challenge #1 with a prize of $1500, the opportunity to pursue a contract with IPS, tickets to Indy 11 games, and meetings with the CEOs of TechPoint and Indy Chamber.
