{
  description = "Provision a dev environment";

  inputs = {
    typelevel-nix.url = "github:rossabaker/typelevel-nix";
    nixpkgs.follows = "typelevel-nix/nixpkgs";
    flake-utils.follows = "typelevel-nix/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils, typelevel-nix }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs {
          inherit system;
          overlays = [ typelevel-nix.overlay ];
        };
        jekyll = pkgs.writeScriptBin "jekyll" ''
          ${pkgs.bundler}/bin/bundle && ${pkgs.bundler}/bin/bundle exec jekyll "$@"
        '';
      in
      {
        devShell = pkgs.devshell.mkShell {
          imports = [ typelevel-nix.typelevel-shell ];
          name = "my-project-shell";
          typelevel-shell.jdk.package = pkgs.jdk8;
          packages = [ jekyll ];
        };
      }
    );
}
