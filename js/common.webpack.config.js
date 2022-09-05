var globalModules = {
  "pixi.js": "pixi.js"
};

const importRule = {
  // Force require global modules
  test: /.*-(fast|full)opt\.js$/,
  use: [
    {
      loader: "imports-loader",
      options: {
        imports: "namespace pixi.js PIXI"
      }
    }
  ]
};

// const exposeRules = Object.keys(globalModules).map(function(modName) {
//   // Expose global modules
//   return {
//     test: require.resolve(modName),
//     loader: "expose-loader",
//     options: {
//       exposes: [globalModules[modName]],
//     },
//   };
// });

// const allRules = exposeRules.concat(importRule);
const allRules = [importRule];

module.exports = {
  performance: { hints: false },
  module: {
    rules: allRules
  }
};