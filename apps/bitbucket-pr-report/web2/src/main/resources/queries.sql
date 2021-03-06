-- deployments (builds on master branch)
SELECT r.SLUG, COUNT(*) c FROM pipeline p
JOIN REPOSITORY r ON p.REPOSITORY_UUID = r."UUID"
JOIN "USER" u ON p.CREATOR_UUID=u."UUID"
WHERE p.TARGET_REF_NAME = 'master'
GROUP BY r.SLUG
ORDER BY c DESC

-- builds (builds on feature branches)
SELECT r.SLUG, COUNT(*) c FROM pipeline p
JOIN REPOSITORY r ON p.REPOSITORY_UUID = r."UUID"
JOIN "USER" u ON p.CREATOR_UUID=u."UUID"
WHERE p.TARGET_REF_NAME != 'master'
GROUP BY r.SLUG
ORDER BY c DESC

-- builds per USER
SELECT u.USERNAME, COUNT(*) c FROM pipeline p
JOIN REPOSITORY r ON p.REPOSITORY_UUID = r."UUID"
JOIN "USER" u ON p.CREATOR_UUID=u."UUID"
GROUP BY u.USERNAME
ORDER BY c DESC

-- PR buddies
SELECT author.USERNAME author_username, APPROVER.USERNAME approver_username, COUNT(*) c FROM PULL_REQUEST pr
JOIN REPOSITORY r ON pr.REPOSITORY_UUID = r."UUID"
JOIN "USER" author ON pr.AUTHOR_UUID = author."UUID"
JOIN PULL_REQUEST_APPROVER pra ON pr.ID = pra.ID AND pr.REPOSITORY_UUID = pra.REPOSITORY_UUID
JOIN "USER" approver ON pra.APPROVER_UUID = APPROVER."UUID"
GROUP BY author.USERNAME, APPROVER.USERNAME


-- PR authors per month
SELECT author.USERNAME author_username, FORMATDATETIME(pr.CREATED_ON, 'yyyy-MM'), COUNT(*) c FROM PULL_REQUEST pr
JOIN REPOSITORY r ON pr.REPOSITORY_UUID = r."UUID"
JOIN "USER" author ON pr.AUTHOR_UUID = author."UUID"
GROUP BY author.USERNAME, FORMATDATETIME(pr.CREATED_ON, 'yyyy-MM')
ORDER BY FORMATDATETIME(pr.CREATED_ON, 'yyyy-MM'), AUTHOR.USERNAME


-- average build duration
SELECT r.SLUG, COUNT(p.DURATION_IN_SECONDS), AVG(p.DURATION_IN_SECONDS) FROM PIPELINE p
JOIN REPOSITORY r
ON p.REPOSITORY_UUID = r."UUID"
WHERE p.STATE = 'COMPLETED' AND p."RESULT" = 'SUCCESSFUL'
GROUP BY r.SLUG
ORDER BY AVG(p.DURATION_IN_SECONDS) DESC

-- most failed builds
SELECT a.*, b.fail_count, (b.fail_count * 100.0 / (b.fail_count + a.success_count)) AS fail_percentage FROM (
	SELECT r.SLUG, COUNT(*) AS success_count FROM REPOSITORY r
	JOIN PIPELINE p
	ON r."UUID" = p.REPOSITORY_UUID
	WHERE p.STATE = 'COMPLETED' AND p."RESULT" = 'SUCCESSFUL'
	GROUP BY r.SLUG
) a JOIN (
	SELECT r.SLUG, COUNT(*) AS fail_count FROM REPOSITORY r
	JOIN PIPELINE p
	ON r."UUID" = p.REPOSITORY_UUID
	WHERE p.STATE != 'COMPLETED' OR p."RESULT" != 'SUCCESSFUL'
	GROUP BY r.SLUG
) b ON a.SLUG=b.SLUG
ORDER BY FAIL_PERCENTAGE DESC
