TARGETS=nao_soccer_player_blue nao_soccer_player_red nao_soccer_supervisor

.PHONY: $(TARGETS)

compil clean:
	@for X in $(TARGETS); do \
	  echo $@ing $$X; \
	  make -s -C $$X $@ ; \
	done
	
