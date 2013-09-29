run:
	lein run -d

js:
	lein cljsbuild auto main

min:
	lein cljsbuild auto min


migrate:
	cd migrations && nomad apply --init -a
