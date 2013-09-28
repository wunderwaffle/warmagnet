run:
	lein run

js:
	lein cljsbuild auto

migrate:
	cd migrations && nomad apply --init -a
