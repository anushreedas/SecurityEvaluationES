(deftemplate MAIN::AndroidSecurity
   (slot AndroidSecurityRisk)
   (slot SoftwareSecurityRisk)
   (slot HardwareSecurityRisk)
   (slot ApplicationSecurityRisk)
   (slot NotLatestAPI)
   (slot NotLatestDate)
   (slot LockSetStatus)
   (slot RootAccessStatus)
   (slot NoOfApps))

(defrule MAIN::API25orless
   (API ?apilevel)
   (test (<= ?apilevel 25))
   =>
   (assert (NotLatestAPI 1.0))
   (assert (LatestAPI 0.0)))

(defrule MAIN::API26
   (API 26)
   =>
   (assert (NotLatestAPI 0.7))
   (assert (LatestAPI 0.3)))

(defrule MAIN::API27
   (API 27)
   =>
   (assert (NotLatestAPI 0.4))
   (assert (LatestAPI 0.6)))

(defrule MAIN::API28ormore
   (API ?apilevel)
   (test (>= ?apilevel 28))
   =>
   (assert (NotLatestAPI 0.0))
   (assert (LatestAPI 1.0)))

(defrule MAIN::DateYear2019
   (PatchDate ?year ?)
   (test (<= ?year 2019))
   =>
   (assert (NotLatestDate 1.0))
   (assert (LatestDate 0.0)))

(defrule MAIN::DateMonthJanorFeb
   (PatchDate 2020 1|2)
   =>
   (assert (NotLatestDate 1.0))
   (assert (LatestDate 0.0)))

(defrule MAIN::DateMonthMarch
   (PatchDate 2020 3)
   =>
   (assert (NotLatestDate 0.7))
   (assert (LatestDate 0.2)))

(defrule MAIN::DateMonthApril
   (PatchDate 2020 4)
   =>
   (assert (NotLatestDate 0.3))
   (assert (LatestDate 0.5)))

(defrule MAIN::DateMonthMayorlater
   (PatchDate 2020 ?month)
   (test (>= ?month 5))
   =>
   (assert (NotLatestDate 0.0))
   (assert (LatestDate 1.0)))

(defrule MAIN::SoftwareSecurityHigh1
   (NotLatestAPI ?x)
   (NotLatestDate ?y)
   (test (>= ?x ?y))
   =>
   (assert (SoftwareSecurityHighRisk ?x)))

(defrule MAIN::SoftwareSecurityHigh2
   (NotLatestAPI ?x)
   (NotLatestDate ?y)
   (test (<= ?x ?y))
   =>
   (assert (SoftwareSecurityHighRisk ?y)))

(defrule MAIN::SoftwareSecurityMedium1
   (NotLatestAPI ?x1)
   (LatestDate ?y1)
   (LatestAPI ?x2)
   (NotLatestDate ?y2)
   =>
   (assert (SoftwareSecurityMediumRisk (min (max ?x1 ?y1) (max ?x2 ?y2)))))

(defrule MAIN::SoftwareSecurityLow1
   (LatestAPI ?x)
   (LatestDate ?y)
   (test (<= ?x ?y))
   =>
   (assert (SoftwareSecurityLowRisk ?x)))

(defrule MAIN::SoftwareSecurityLow2
   (LatestAPI ?x)
   (LatestDate ?y)
   (test (>= ?x ?y))
   =>
   (assert (SoftwareSecurityLowRisk ?y)))

(defrule MAIN::SoftwareSecurityRiskRate
   (SoftwareSecurityLowRisk ?x)
   (SoftwareSecurityMediumRisk ?y)
   (SoftwareSecurityHighRisk ?z)
   =>
   (assert (SoftwareSecurityRisk (/ (+ (* ?x 30) (* ?y 180) (* ?z 340)) (+ (* ?x 3) (* ?y 4) (* ?z 4))))))

(defrule MAIN::HardwareSecurityLow
   (LockSetStatus true)
   (RootAccessStatus true)
   =>
   (assert (HardwareSecurityRisk 0.0)))

(defrule MAIN::HardwareSecurityMedium1
   (LockSetStatus false)
   (RootAccessStatus true)
   =>
   (assert (HardwareSecurityRisk 50.0)))

(defrule MAIN::HardwareSecurityMedium2
   (LockSetStatus true)
   (RootAccessStatus false)
   =>
   (assert (HardwareSecurityRisk 50.0)))

(defrule MAIN::HardwareSecurityHigh
   (LockSetStatus false)
   (RootAccessStatus false)
   =>
   (assert (HardwareSecurityRisk 100.0)))

(defrule MAIN::ApplicationSecurity0
   (NoOfApps 0)
   =>
   (assert (ApplicationSecurityRisk 0)))

(defrule MAIN::ApplicationSecurity123
   (NoOfApps 1|2|3)
   =>
   (assert (ApplicationSecurityRisk 30)))

(defrule MAIN::ApplicationSecurity456
   (NoOfApps 4|5|6)
   =>
   (assert (ApplicationSecurityRisk 65)))

(defrule MAIN::ApplicationSecurity7ormore
   (NoOfApps ?num)
   (test (>= ?num 7))
   =>
   (assert (ApplicationSecurityRisk 100)))

(defrule MAIN::AndroidSecurityRate
   (SoftwareSecurityRisk ?x)
   (HardwareSecurityRisk ?y)
   (ApplicationSecurityRisk ?z)
   (NotLatestAPI ?a)
   (NotLatestDate ?b)
   (LockSetStatus ?c)
   (RootAccessStatus ?d)
   (NoOfApps ?e)
   =>
  (assert (AndroidSecurity (AndroidSecurityRisk (+  (* ?x 0.4) (* ?y 0.4) (* ?z 0.2))) (SoftwareSecurityRisk ?x) (HardwareSecurityRisk ?y) (ApplicationSecurityRisk ?z) (NotLatestAPI ?a) (NotLatestDate ?b) (LockSetStatus ?c) (RootAccessStatus ?d) (NoOfApps ?e))))

